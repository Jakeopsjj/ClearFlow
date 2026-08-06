package com.cleardu.app

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.cleardu.app.data.PermissionCatalog
import com.cleardu.app.data.PermissionItem
import com.cleardu.app.ui.screens.OnboardingScreen
import com.cleardu.app.ui.screens.SplashScreen
import com.cleardu.app.ui.theme.ClearDuTheme
import com.cleardu.app.util.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Launcher Activity — first entry point of the app.
 *
 * Flow:
 *  1. Check DataStore for [KEY_ONBOARDING_COMPLETE].
 *  2. If onboarding is NOT complete → show the onboarding screen (welcome + permissions).
 *  3. If onboarding IS complete → show a brief branded splash (like WeChat earth),
 *     then navigate to [MainActivity].
 *
 * Onboarding features:
 *  - Warm welcome message with app logo
 *  - 6 permission cards; each taps to request the corresponding runtime permission
 *  - "一键授予全部权限" button that sequentially requests all ungranted permissions
 *  - "开始使用" marks onboarding as complete and launches [MainActivity]
 */
class OnboardingActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    /** [修改点] 多权限同时申请 Launcher，用于 Android 12+ 定位 FINE + COARSE 同时申请 */
    private lateinit var multiPermissionLauncher: ActivityResultLauncher<Array<String>>

    private val grantedState = mutableStateMapOf<String, Boolean>()
    private val pendingItemFlow = MutableStateFlow<PermissionItem?>(null)

    /** Whether the sequential "grant all" flow is in progress. */
    private var isGrantingAll by mutableStateOf(false)

    /** Queue of permissions to request during the "grant all" flow. */
    private val grantAllQueue = ArrayDeque<PermissionItem>()

    /** Whether onboarding has been completed (read from DataStore). */
    private var onboardingComplete by mutableStateOf<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            val item = pendingItemFlow.value ?: return@registerForActivityResult
            grantedState[item.id] = granted
            pendingItemFlow.value = null

            // [修改点] 定位权限拒绝友好提示，不崩溃
            if (!granted && item.id == "location") {
                Toast.makeText(
                    this,
                    "定位权限被拒绝，部分功能（如附近医院、紧急定位）将无法使用。" +
                        "可稍后在系统设置中重新授予。",
                    Toast.LENGTH_LONG
                ).show()
            }

            // If we're in "grant all" mode, request the next permission
            if (isGrantingAll) {
                requestNextInQueue()
            }
        }

        // [修改点] 多权限 Launcher：Android 12+ 定位需同时申请 FINE + COARSE
        multiPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val item = pendingItemFlow.value ?: return@registerForActivityResult
            // 定位权限：至少 COARSE 授予即视为成功（FINE 是精确位置升级项）
            val granted = results.values.any { it }
            grantedState[item.id] = granted
            pendingItemFlow.value = null

            // [修改点] 权限拒绝友好提示
            if (!granted && item.id == "location") {
                Toast.makeText(
                    this,
                    "定位权限被拒绝，部分功能（如附近医院、紧急定位）将无法使用。" +
                        "可稍后在系统设置中重新授予。",
                    Toast.LENGTH_LONG
                ).show()
            }

            if (isGrantingAll) {
                requestNextInQueue()
            }
        }

        // Seed initial state from the actual granted permissions.
        refreshGrantedState()

        setContent {
            ClearDuTheme {
                OnboardingRoot()
            }
        }
    }

    @Composable
    private fun OnboardingRoot() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Read onboarding completion state from DataStore
            LaunchedEffect(Unit) {
                val prefs = applicationContext.dataStore.data.first()
                onboardingComplete = prefs[KEY_ONBOARDING_COMPLETE] == true
            }

            when (onboardingComplete) {
                null -> {
                    // Still loading DataStore — show nothing (system splash is still visible)
                }
                true -> {
                    // Onboarding already done → show branded splash, then go to MainActivity
                    SplashScreen(
                        onSplashFinished = ::startMainActivity
                    )
                }
                false -> {
                    // First launch → show onboarding
                    OnboardingScreen(
                        grantedMap = grantedState,
                        isGrantingAll = isGrantingAll,
                        onAllowPermission = ::handleAllowPermission,
                        onGrantAll = ::startGrantAll,
                        onStart = ::onOnboardingComplete
                    )
                }
            }
        }
    }

    // ---- Permission handling ----

    private fun handleAllowPermission(item: PermissionItem) {
        if (grantedState[item.id] == true) return
        requestPermission(item)
    }

    /**
     * "一键授予全部权限" — collect all un-granted permissions and request them
     * one by one using the sequential queue.
     */
    private fun startGrantAll() {
        if (isGrantingAll) return

        // Collect all un-granted permissions
        val unGranted = PermissionCatalog.items.filter { grantedState[it.id] != true }
        if (unGranted.isEmpty()) {
            isGrantingAll = false
            return
        }

        isGrantingAll = true
        grantAllQueue.clear()
        grantAllQueue.addAll(unGranted)

        // Start requesting the first one
        requestNextInQueue()
    }

    /** Request the next permission in the grant-all queue. */
    private fun requestNextInQueue() {
        val next = grantAllQueue.removeFirstOrNull()
        if (next != null) {
            requestPermission(next)
        } else {
            // All done
            isGrantingAll = false
        }
    }

    private fun requestPermission(item: PermissionItem) {
        when (item.id) {
            // [修改点] Android 12+ 同时申请 FINE + COARSE，系统才弹"精确/粗略"选择弹窗；
            //         Android 11 及以下仅申请 FINE（COARSE 隐含在 FINE 中）
            "location" -> {
                val perms = LocationHelper.requiredLocationPermissions()
                if (perms.size > 1) {
                    requestRuntimePermissions(item, perms)
                } else {
                    requestRuntimePermission(item, perms[0])
                }
            }
            "phone" -> requestRuntimePermission(item, Manifest.permission.CALL_PHONE)
            "contacts" -> requestRuntimePermission(item, Manifest.permission.READ_CONTACTS)
            "storage" -> {
                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                requestRuntimePermission(item, perm)
            }
            "alert" -> {
                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else {
                    grantedState[item.id] = true
                    // If in grant-all mode, continue to next
                    if (isGrantingAll) requestNextInQueue()
                    return
                }
                requestRuntimePermission(item, perm)
            }
            "alarm" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(AlarmManager::class.java)
                    if (alarmManager?.canScheduleExactAlarms() == true) {
                        grantedState[item.id] = true
                    } else {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData(Uri.parse("package:$packageName"))
                            startActivity(intent)
                            // Can't track result; mark as "handled" for UX
                            grantedState[item.id] = true
                        } catch (_: Exception) {
                            grantedState[item.id] = true
                        }
                    }
                } else {
                    grantedState[item.id] = true
                }
                // If in grant-all mode, continue to next
                if (isGrantingAll) requestNextInQueue()
            }
            "apps" -> {
                // QUERY_ALL_PACKAGES is manifest-only
                grantedState[item.id] = true
                // If in grant-all mode, continue to next
                if (isGrantingAll) requestNextInQueue()
            }
        }
    }

    private fun requestRuntimePermission(item: PermissionItem, permission: String) {
        val granted = ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            grantedState[item.id] = true
            // If in grant-all mode, continue to next
            if (isGrantingAll) requestNextInQueue()
            return
        }
        pendingItemFlow.value = item
        permissionLauncher.launch(permission)
    }

    /**
     * [修改点] 同时申请多个运行时权限。
     * 用于 Android 12+ 定位：必须同时申请 FINE + COARSE，
     * 系统才会弹出"精确位置/粗略位置"选择弹窗。
     */
    private fun requestRuntimePermissions(item: PermissionItem, permissions: Array<String>) {
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            grantedState[item.id] = true
            if (isGrantingAll) requestNextInQueue()
            return
        }
        pendingItemFlow.value = item
        multiPermissionLauncher.launch(permissions)
    }

    private fun refreshGrantedState() {
        fun check(permission: String): Boolean =
            ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED

        // [修改点] Android 12+ 用户可能只授予 COARSE（粗略定位），也应视为已授权
        grantedState["location"] = LocationHelper.hasAnyLocationPermission(this)
        grantedState["phone"] = check(Manifest.permission.CALL_PHONE)
        grantedState["contacts"] = check(Manifest.permission.READ_CONTACTS)
        grantedState["storage"] = check(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else Manifest.permission.READ_EXTERNAL_STORAGE
        )
        grantedState["alert"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            check(Manifest.permission.POST_NOTIFICATIONS)
        } else true

        grantedState["alarm"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            am?.canScheduleExactAlarms() == true
        } else true

        grantedState["apps"] = true // manifest-only
    }

    // ---- Navigation ----

    /** Called when the user taps "开始使用" on the onboarding screen. */
    private fun onOnboardingComplete() {
        // Persist the onboarding-complete flag
        lifecycleScope.launch(Dispatchers.IO) {
            applicationContext.dataStore.edit { prefs ->
                prefs[KEY_ONBOARDING_COMPLETE] = true
            }
        }
        startMainActivity()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Returning from a system permission screen (e.g. exact alarm Settings)
        refreshGrantedState()
    }
}