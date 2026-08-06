package com.cleardu.app.util.map

import android.app.Application
import android.util.Log

/**
 * 三家地图 SDK 总调度管理器。
 *
 * 在 Application.onCreate 中调用 [init] 完成全部 SDK 初始化。
 * 每家 SDK 独立 try-catch，单家初始化异常只标记不可用，不抛出崩溃。
 *
 * 地图视图渲染降级顺序：高德 → 百度 → 腾讯
 * POI 搜索降级顺序：百度 → 高德 → 腾讯
 * 定位降级顺序：高德 → 百度 → 腾讯 → 系统原生
 * 地理编码降级顺序：高德 → 百度 → 腾讯
 */
object MapServiceManager {

    private const val TAG = "MapServiceManager"

    /** 高德 SDK 是否可用 */
    @Volatile var amapAvailable = false
        private set

    /** 百度 SDK 是否可用 */
    @Volatile var baiduAvailable = false
        private set

    /** 腾讯 SDK 是否可用 */
    @Volatile var tencentAvailable = false
        private set

    /** 是否已完成初始化 */
    @Volatile var initialized = false
        private set

    /**
     * 初始化全部三家地图 SDK。
     * 必须在 Application.onCreate 中调用，且必须在主线程。
     */
    fun init(app: Application) {
        if (initialized) {
            Log.w(TAG, "MapServiceManager 已初始化，跳过重复调用")
            return
        }

        Log.i(TAG, "===== 开始初始化三家地图 SDK =====")

        // 1) 高德 SDK
        try {
            com.amap.api.maps.MapsInitializer.initialize(app)
            // 设置隐私合规（同意隐私政策后才能正常使用）
            com.amap.api.maps.MapsInitializer.updatePrivacyShow(app, true, true)
            com.amap.api.maps.MapsInitializer.updatePrivacyAgree(app, true)
            amapAvailable = true
            Log.i(TAG, "【高德】SDK 初始化成功")
        } catch (e: Exception) {
            amapAvailable = false
            Log.e(TAG, "【高德】SDK 初始化失败，标记不可用", e)
        }

        // 2) 百度 SDK
        try {
            com.baidu.mapapi.SDKInitializer.setAgreePrivacy(app, true)
            com.baidu.mapapi.SDKInitializer.initialize(app)
            baiduAvailable = true
            Log.i(TAG, "【百度】SDK 初始化成功")
        } catch (e: Exception) {
            baiduAvailable = false
            Log.e(TAG, "【百度】SDK 初始化失败，标记不可用", e)
        }

        // 3) 腾讯 SDK
        try {
            // 腾讯地图 SDK 初始化通过 TencentMapInitializer（仅需设置隐私协议）
            com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer.setAgreePrivacy(true)
            tencentAvailable = true
            Log.i(TAG, "【腾讯】SDK 初始化成功")
        } catch (e: Exception) {
            tencentAvailable = false
            Log.e(TAG, "【腾讯】SDK 初始化失败，标记不可用", e)
        }

        initialized = true
        Log.i(
            TAG,
            "===== SDK 初始化完成: 高德=$amapAvailable, 百度=$baiduAvailable, 腾讯=$tencentAvailable ====="
        )
    }

    /**
     * 获取地图视图渲染优先级列表（降序）。
     * 第一个可用即使用。
     */
    fun mapViewPriority(): List<MapVendor> = listOfNotNull(
        if (amapAvailable) MapVendor.AMAP else null,
        if (baiduAvailable) MapVendor.BAIDU else null,
        if (tencentAvailable) MapVendor.TENCENT else null
    )

    /**
     * 检查是否至少有一家 SDK 可用。
     */
    fun anyAvailable(): Boolean = amapAvailable || baiduAvailable || tencentAvailable
}

/** 地图服务商标识 */
enum class MapVendor {
    AMAP,    // 高德
    BAIDU,   // 百度
    TENCENT  // 腾讯
}