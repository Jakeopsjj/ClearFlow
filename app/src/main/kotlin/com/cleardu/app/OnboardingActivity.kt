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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cleardu.app.data.PermissionItem
import com.cleardu.app.ui.screens.OnboardingScreen
import com.cleardu.app.ui.theme.ClearDuTheme
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Single-page onboarding / permission screen.
 *
 * Architecture:
 *  - Splash via androidx.core.splashscreen
 *  - Edge-to-edge with light (white) system bar icons over the dark mesh bg
 *  - Permission grant state stored in a [SnapshotStateMap] so Compose recomposes
 *    when the user grants / denies a permission
 *  - Each "允许" tap routes to the matching runtime permission request (or, for
 *    manifest-only / special permissions, updates state directly)
 *  - "开始使用" launches [MainActivity] and finishes this Activity
 */
class OnboardingActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val grantedState = mutableStateMapOf<String, Boolean>()
    private val pendingItemFlow = MutableStateFlow<PermissionItem?>(null)

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
        }

        // Seed initial state from the actual granted permissions so the UI reflects reality.
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
            OnboardingScreen(
                grantedMap = grantedState,
                onAllowPermission = ::handleAllowPermission,
                onStart = ::startMainActivity
            )
        }
    }

    private fun handleAllowPermission(item: PermissionItem) {
        // If already granted, no-op (matches the HTML `if (card.granted) return`).
        if (grantedState[item.id] == true) return

        when (item.id) {
            "location" -> requestRuntimePermission(
                item,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            "phone" -> requestRuntimePermission(item, Manifest.permission.CALL_PHONE)
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
                    // Notifications don't need a runtime permission below API 33.
                    grantedState[item.id] = true
                    return
                }
                requestRuntimePermission(item, perm)
            }
            "alarm" -> {
                // SCHEDULE_EXACT_ALARM is a special permission: must route through Settings.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(AlarmManager::class.java)
                    if (alarmManager?.canScheduleExactAlarms() == true) {
                        grantedState[item.id] = true
                    } else {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData(Uri.parse("package:$packageName"))
                            startActivity(intent)
                        } catch (_: Exception) {
                            // No settings UI available — fall back to marking as requested.
                            grantedState[item.id] = true
                        }
                    }
                } else {
                    grantedState[item.id] = true
                }
            }
            "apps" -> {
                // QUERY_ALL_PACKAGES is a manifest-declared permission with no runtime grant.
                // Mark as allowed; the actual policy is enforced at install time.
                grantedState[item.id] = true
            }
        }
    }

    private fun requestRuntimePermission(item: PermissionItem, permission: String) {
        val granted = ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            grantedState[item.id] = true
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
        // should re-read the live grant state so the UI updates.
        refreshGrantedState()
    }
}
