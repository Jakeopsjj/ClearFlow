package com.cleardu.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cleardu.app.util.map.MapServiceManager
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
 *
 * 初始化三家地图 SDK（高德/百度/腾讯），每家独立 try-catch，
 * 单家异常只标记不可用，不抛出崩溃。
 */
class ClearDuApplication : Application() {

    companion object {
        lateinit var instance: ClearDuApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化 OSMDroid 配置（保留作为无 SDK 环境的备用方案）
        Configuration.getInstance().apply {
            userAgentValue = "ClearDu/${packageManager.getPackageInfo(packageName, 0).versionName}"
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir.resolve("osmdroid")
        }

        // 初始化三家地图 SDK
        MapServiceManager.init(this)
    }
}