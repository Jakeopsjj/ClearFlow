package com.cleardu.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import org.osmdroid.config.Configuration

/** Top-level DataStore singleton — one instance per process. */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cleardu_prefs")

/** Key for onboarding-complete flag. */
val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

/**
 * Application entry point.
 *
 * Provides a singleton reference for easy access to the Application context
 * and its DataStore from any component.
 */
class ClearDuApplication : Application() {

    companion object {
        lateinit var instance: ClearDuApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化 OSMDroid 配置：使用内部缓存目录存储瓦片，无需额外存储权限
        Configuration.getInstance().apply {
            userAgentValue = "ClearDu/${packageManager.getPackageInfo(packageName, 0).versionName}"
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir.resolve("osmdroid")
        }
    }
}