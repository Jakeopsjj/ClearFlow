package com.cleardu.app

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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

            // If we're in "grant all" mode, request the next permission
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
            "location" -> requestRuntimePermission(
                item, Manifest.permission.ACCESS_FINE_LOCATION
            )
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

    private fun refreshGrantedState() {
        fun check(permission: String): Boolean =
            ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED

        grantedState["location"] = check(Manifest.permission.ACCESS_FINE_LOCATION)
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