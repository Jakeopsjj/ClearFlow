package com.cleardu.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

/**
 * LocationHelper —— 前台定位统一封装（仅 LocationManager，不依赖 Play Services）。
 *
 * ## 设计要点（修复定位回调缺失 / 精度差）
 *
 * 1. 运行时权限：Android 12+ 必须同时申请 FINE + COARSE；本类只负责"拿到权限后"的调用。
 * 2. 3 层校验：
 *    - ① 系统全局定位总开关（[LocationManager.isLocationEnabled]，API 28+）
 *    - ② Android 12+ 精确位置开关（仅 FINE 被授予但 COARSE 未授予 ⇒ 只能粗定位）
 *    - ③ GPS_PROVIDER / NETWORK_PROVIDER 可用状态日志
 * 3. 双 Provider 注册：同时注册 GPS + NETWORK，室内靠 NETWORK 兜底。
 * 4. getLastKnownLocation 缓存过滤：超过 [MAX_CACHE_AGE_HOURS] 小时直接丢弃。
 * 5. 生命周期：[destroy] 必须在 Activity.onDestroy 调用，Android 14+ 不注销会被系统冻结。
 * 6. 仅前台定位，不声明 ACCESS_BACKGROUND_LOCATION。
 * 7. 国产 ROM 提示：长时间无回调时 Toast 提醒用户把电池策略设为"无限制"。
 *
 * 兼容：API 26 (Android 8) ~ API 36 (Android 16)
 * 适配 ROM：realmeUI / HyperOS / ColorOS / OriginOS / MagicOS
 */
class LocationHelper private constructor(
    private val context: Context
) {

    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    /** [修改点] 主线程 Handler，用于超时定时器，复用同一个实例避免泄漏。 */
    private val mainHandler = Handler(Looper.getMainLooper())

    /** [修改点] 超时 Runnable 引用，便于在拿到位置后取消，防止泄漏 + 重复回调。 */
    private var timeoutRunnable: Runnable? = null

    /** [修改点] 标记本次请求结果是否已投递，防止缓存 + 实时更新双重回调。 */
    private var resultDelivered = false

    /** 当前注册中的 listener，便于 [destroy] 注销。 */
    private var activeListener: LocationListener? = null

    /** 是否已提示过国产 ROM 电池策略 Toast（每次请求周期内只提示一次）。 */
    private var romHintShown = false

    /** 上一次回调时间戳（用于检测长时间无回调）。 */
    private var lastCallbackElapsedRealtime = 0L

    /**
     * 定位结果状态。
     */
    sealed class Result {
        /** 成功获取位置。 */
        data class Success(val location: Location) : Result()
        /** 系统全局定位总开关关闭。 */
        data object LocationDisabled : Result()
        /** Android 12+ 精确位置未开启，仅粗定位可用（已返回粗定位位置）。 */
        data class CoarseOnly(val location: Location) : Result()
        /** 权限未授予。 */
        data object PermissionDenied : Result()
        /** 超时未获取到位置。 */
        data object Timeout : Result()
        /** LocationManager 不可用（极端情况）。 */
        data object NoManager : Result()
    }

    // ============================================================
    //  公开 API
    // ============================================================

    /**
     * 检查运行时定位权限是否已授予。
     *
     * Android 12+：FINE 和 COARSE 必须至少有一个；优先 FINE。
     */
    fun hasLocationPermission(): Boolean {
        val fine = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+：至少 COARSE 必须有（FINE 是升级项）
            fine || coarse
        } else {
            // Android 11 及以下：只需 FINE
            fine
        }
    }

    /**
     * 是否拥有精确定位权限（FINE + COARSE 同时授予）。
     * Android 12+ 精确位置开关实际反映在权限粒度上。
     */
    fun hasFineLocation(): Boolean =
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    /**
     * 启动单次定位请求。
     *
     * 流程：3 层校验 → 读缓存 → 注册 GPS+NETWORK 双 Provider 监听 → 超时兜底。
     *
     * @param timeoutMs 超时时间（毫秒），超时后回调 [Result.Timeout]
     * @param onResult 主线程回调
     */
    @SuppressLint("MissingPermission")
    fun requestSingleUpdate(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        onResult: (Result) -> Unit
    ) {
        android.util.Log.i(TAG, "===== requestSingleUpdate start =====")
        logPermissionState()

        // [修改点] 每次请求前重置状态，防止上一次请求的残留标志影响本次
        resultDelivered = false
        cancelTimeout()

        // ---------- 校验 0：LocationManager 是否存在 ----------
        if (locationManager == null) {
            android.util.Log.e(TAG, "LocationManager is null")
            onResult(Result.NoManager)
            return
        }

        // ---------- 校验 1：系统全局定位总开关 ----------
        if (!isLocationEnabled()) {
            android.util.Log.w(TAG, "【校验1失败】系统定位总开关关闭")
            onResult(Result.LocationDisabled)
            return
        }

        // ---------- 校验 2：运行时权限 ----------
        if (!hasLocationPermission()) {
            android.util.Log.w(TAG, "【校验2失败】运行时定位权限未授予")
            onResult(Result.PermissionDenied)
            return
        }

        // ---------- 校验 3：Provider 状态日志 ----------
        val gpsEnabled = isProviderEnabledSafe(LocationManager.GPS_PROVIDER)
        val networkEnabled = isProviderEnabledSafe(LocationManager.NETWORK_PROVIDER)
        android.util.Log.i(
            TAG,
            "【校验3-Provider】GPS_PROVIDER=${if (gpsEnabled) "ON" else "OFF"}, " +
                "NETWORK_PROVIDER=${if (networkEnabled) "ON" else "OFF"}"
        )

        if (!gpsEnabled && !networkEnabled) {
            android.util.Log.w(TAG, "GPS 和 NETWORK 两个 Provider 都关闭，无法定位")
            onResult(Result.LocationDisabled)
            return
        }

        // ---------- Android 12+ 精确位置开关检测 ----------
        val fineOnly = !hasFineLocation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && fineOnly) {
            android.util.Log.w(
                TAG,
                "【精确位置开关】Android 12+ 仅授予 COARSE，未开启精确位置；只能粗定位"
            )
            // [修改点] 提示用户去开启精确位置，否则只能粗定位
            Toast.makeText(
                appContext,
                "当前仅粗略定位可用。如需精确定位，请前往系统设置 → 应用 → ClearDu → " +
                    "权限 → 位置信息，开启【精确位置】",
                Toast.LENGTH_LONG
            ).show()
        }

        // ---------- 尝试读取缓存 ----------
        val cached = getCachedLocation()
        if (cached != null) {
            android.util.Log.i(
                TAG,
                "使用缓存位置: lat=${cached.latitude}, lng=${cached.longitude}, " +
                    "accuracy=${cached.accuracy}m, age=${cacheAgeMinutes(cached)}min"
            )
            val result = if (cached.accuracy > COARSE_ACCURACY_THRESHOLD_M) {
                if (fineOnly) Result.CoarseOnly(cached) else Result.Success(cached)
            } else {
                Result.Success(cached)
            }
            // [修改点] 有缓存立即返回，不再注册监听，避免重复回调和无效耗电
            deliverResult(result, onResult)
            return
        }

        // ---------- 无缓存，注册双 Provider 监听 ----------
        startLocationUpdates(timeoutMs, fineOnly, onResult)
    }

    /**
     * 跳转系统定位设置页（用户拒绝/关闭定位时引导）。
     */
    fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            appContext.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "无法跳转定位设置页", e)
            Toast.makeText(
                appContext,
                "请前往系统设置 → 位置信息，开启定位功能",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * [修改点] 跳转本应用权限设置页（Android 12+ 引导用户开启【精确位置】开关）。
     */
    fun openAppPermissionSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", appContext.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            appContext.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "无法跳转应用权限设置页", e)
            Toast.makeText(
                appContext,
                "请前往系统设置 → 应用 → ClearDu → 权限，手动开启定位权限",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * 必须在 Activity.onDestroy 调用，彻底注销监听。
     *
     * Android 14+（API 34）不注销 LocationListener 会被系统永久冻结定位回调，
     * 且在后台耗电，导致后续无法获取位置。
     */
    fun destroy() {
        android.util.Log.i(TAG, "destroy() — 注销所有 LocationListener")
        stopLocationUpdates()
    }

    // ============================================================
    //  内部实现
    // ============================================================

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(
        timeoutMs: Long,
        fineOnly: Boolean,
        onResult: (Result) -> Unit
    ) {
        // 先注销可能存在的旧监听
        stopLocationUpdates()
        romHintShown = false
        lastCallbackElapsedRealtime = SystemClock.elapsedRealtime()

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                logLocation("onLocationChanged", location)
                lastCallbackElapsedRealtime = SystemClock.elapsedRealtime()

                // [修改点] 精度判断后通过 deliverResult 投递，自动注销监听 + 取消超时
                val isCoarse = location.accuracy > COARSE_ACCURACY_THRESHOLD_M
                val result = when {
                    fineOnly && isCoarse -> Result.CoarseOnly(location)
                    else -> Result.Success(location)
                }
                deliverResult(result, onResult)
            }

            // 兼容旧 API（Android 8-9）
            @Deprecated("legacy", ReplaceWith(""))
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                android.util.Log.i(TAG, "onStatusChanged: provider=$provider, status=$status")
            }

            override fun onProviderEnabled(provider: String) {
                android.util.Log.i(TAG, "onProviderEnabled: $provider")
            }

            override fun onProviderDisabled(provider: String) {
                android.util.Log.w(TAG, "onProviderDisabled: $provider")
            }
        }
        activeListener = listener

        val minIntervalMs = MIN_INTERVAL_MS
        val minDistanceM = MIN_DISTANCE_M

        // 同时注册 GPS + NETWORK（不能用 ?:，必须分别注册）
        try {
            if (isProviderEnabledSafe(LocationManager.GPS_PROVIDER) &&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minIntervalMs,
                    minDistanceM,
                    listener
                )
                android.util.Log.i(TAG, "已注册 GPS_PROVIDER 监听")
            }
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "注册 GPS_PROVIDER 失败（权限）", e)
        } catch (e: IllegalArgumentException) {
            android.util.Log.e(TAG, "注册 GPS_PROVIDER 失败（不支持）", e)
        }

        try {
            if (isProviderEnabledSafe(LocationManager.NETWORK_PROVIDER) &&
                (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
            ) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minIntervalMs,
                    minDistanceM,
                    listener
                )
                android.util.Log.i(TAG, "已注册 NETWORK_PROVIDER 监听")
            }
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "注册 NETWORK_PROVIDER 失败（权限）", e)
        } catch (e: IllegalArgumentException) {
            android.util.Log.e(TAG, "注册 NETWORK_PROVIDER 失败（不支持）", e)
        }

        // 超时兜底 + 国产 ROM 提示
        scheduleTimeout(timeoutMs, onResult)
    }

    /**
     * [修改点] 超时定时器，复用 mainHandler + 存储 Runnable 便于取消。
     * 超时后通过 deliverResult 投递，自动注销监听。
     */
    private fun scheduleTimeout(timeoutMs: Long, onResult: (Result) -> Unit) {
        cancelTimeout()
        val runnable = Runnable {
            if (!resultDelivered && activeListener != null) {
                val sinceLast = SystemClock.elapsedRealtime() - lastCallbackElapsedRealtime
                android.util.Log.w(
                    TAG,
                    "定位超时 ${timeoutMs}ms，距上次回调 ${sinceLast}ms，仍未获取位置"
                )

                // 国产 ROM 提示
                if (!romHintShown) {
                    romHintShown = true
                    showDomesticRomHint()
                }

                deliverResult(Result.Timeout, onResult)
            }
        }
        timeoutRunnable = runnable
        mainHandler.postDelayed(runnable, timeoutMs)
    }

    /** [修改点] 取消超时定时器，防止泄漏和超时后重复回调。 */
    private fun cancelTimeout() {
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        timeoutRunnable = null
    }

    /**
     * [修改点] 投递定位结果，保证单次请求只回调一次。
     * 同时注销监听 + 取消超时，彻底清理资源。
     */
    private fun deliverResult(result: Result, onResult: (Result) -> Unit) {
        if (resultDelivered) {
            android.util.Log.d(TAG, "结果已投递，忽略重复回调: $result")
            return
        }
        resultDelivered = true
        stopLocationUpdates()
        onResult(result)
    }

    /** 注销所有监听。 */
    private fun stopLocationUpdates() {
        // [修改点] 同时取消超时定时器
        cancelTimeout()
        activeListener?.let { listener ->
            try {
                locationManager?.removeUpdates(listener)
                android.util.Log.i(TAG, "已 removeUpdates() 注销 LocationListener")
            } catch (e: SecurityException) {
                android.util.Log.e(TAG, "removeUpdates 失败", e)
            }
        }
        activeListener = null
    }

    /**
     * 读取 getLastKnownLocation 缓存，过滤超过 2 小时的过期数据。
     */
    @SuppressLint("MissingPermission")
    private fun getCachedLocation(): Location? {
        if (!hasLocationPermission()) return null

        val gpsCached = safeGetLastKnown(LocationManager.GPS_PROVIDER)
        val networkCached = safeGetLastKnown(LocationManager.NETWORK_PROVIDER)

        // 取两者中较新的
        val candidate = chooseNewer(gpsCached, networkCached) ?: return null

        val ageMs = System.currentTimeMillis() - candidate.time
        return if (ageMs > MAX_CACHE_AGE_MS) {
            android.util.Log.i(
                TAG,
                "缓存位置已过期：age=${TimeUnit.MILLISECONDS.toMinutes(ageMs)}min > " +
                    "${MAX_CACHE_AGE_HOURS}h，丢弃"
            )
            null
        } else {
            candidate
        }
    }

    @SuppressLint("MissingPermission")
    private fun safeGetLastKnown(provider: String): Location? {
        return try {
            if (isProviderEnabledSafe(provider) && hasLocationPermission()) {
                locationManager?.getLastKnownLocation(provider)
            } else null
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "getLastKnownLocation($provider) 权限失败", e)
            null
        } catch (e: IllegalArgumentException) {
            android.util.Log.e(TAG, "getLastKnownLocation($provider) provider不存在", e)
            null
        }
    }

    /** 系统全局定位总开关。 */
    private fun isLocationEnabled(): Boolean {
        // API 28+ 用 isLocationEnabled；低版本用 isProviderEnabled 任一
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager?.isLocationEnabled ?: false
        } else {
            isProviderEnabledSafe(LocationManager.GPS_PROVIDER) ||
                isProviderEnabledSafe(LocationManager.NETWORK_PROVIDER)
        }
    }

    private fun isProviderEnabledSafe(provider: String): Boolean {
        return try {
            locationManager?.isProviderEnabled(provider) ?: false
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun checkPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(appContext, permission) ==
            PackageManager.PERMISSION_GRANTED

    private fun chooseNewer(a: Location?, b: Location?): Location? {
        return when {
            a == null -> b
            b == null -> a
            a.time >= b.time -> a
            else -> b
        }
    }

    private fun cacheAgeMinutes(loc: Location): Long =
        TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - loc.time)

    /**
     * 国产 ROM 电池策略提示。
     *
     * HyperOS / ColorOS / OriginOS / MagicOS / realmeUI 默认会限制后台冻结，
     * 导致长时间无定位回调。提示用户把电池策略设为"无限制"。
     */
    private fun showDomesticRomHint() {
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val brand = Build.BRAND?.lowercase() ?: ""
        val isDomesticRom = manufacturer.contains("xiaomi") ||
            manufacturer.contains("redmi") ||
            manufacturer.contains("oppo") ||
            manufacturer.contains("vivo") ||
            manufacturer.contains("realme") ||
            manufacturer.contains("oneplus") ||
            manufacturer.contains("iqoo") ||
            manufacturer.contains("honor") ||
            manufacturer.contains("huawei") ||
            brand.contains("xiaomi") ||
            brand.contains("redmi") ||
            brand.contains("oppo") ||
            brand.contains("vivo") ||
            brand.contains("realme") ||
            brand.contains("honor")

        val msg = if (isDomesticRom) {
            "长时间未获取到位置。请在系统设置 → 应用管理 → ClearDu → " +
                "电池策略，选择【无限制】或【无限制后台活动】，并关闭【后台冻结】"
        } else {
            "长时间未获取到位置。请检查系统定位是否开启，" +
                "或在系统设置中将本应用电池策略设为【无限制】"
        }

        android.util.Log.w(TAG, "国产ROM提示: $msg (manufacturer=$manufacturer, brand=$brand)")
        Toast.makeText(appContext, msg, Toast.LENGTH_LONG).show()
    }

    // ============================================================
    //  日志
    // ============================================================

    private fun logPermissionState() {
        val fine = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        android.util.Log.i(
            TAG,
            "权限状态: ACCESS_FINE_LOCATION=$fine, ACCESS_COARSE_LOCATION=$coarse, " +
                "SDK=${Build.VERSION.SDK_INT}, MANUFACTURER=${Build.MANUFACTURER}, " +
                "BRAND=${Build.BRAND}, MODEL=${Build.MODEL}"
        )
    }

    private fun logLocation(tag: String, loc: Location) {
        val ageMin = cacheAgeMinutes(loc)
        val accuracyStr = if (loc.accuracy > COARSE_ACCURACY_THRESHOLD_M) {
            "粗定位(>500m)"
        } else {
            "精确定位"
        }
        android.util.Log.i(
            TAG,
            "$tag: lat=${loc.latitude}, lng=${loc.longitude}, " +
                "accuracy=${loc.accuracy}m [$accuracyStr], " +
                "provider=${loc.provider}, time=${loc.time} (age=${ageMin}min)"
        )
    }

    companion object {
        private const val TAG = "LocationHelper"

        /** 单次定位默认超时。 */
        private const val DEFAULT_TIMEOUT_MS = 15_000L

        /** 最小更新间隔。 */
        private const val MIN_INTERVAL_MS = 2_000L

        /** 最小更新距离（米）。 */
        private const val MIN_DISTANCE_M = 0f

        /** 缓存最大有效期（2 小时）。 */
        private const val MAX_CACHE_AGE_HOURS = 2L
        private val MAX_CACHE_AGE_MS = TimeUnit.HOURS.toMillis(MAX_CACHE_AGE_HOURS)

        /** 粗定位精度阈值（>500m 视为粗定位）。 */
        private const val COARSE_ACCURACY_THRESHOLD_M = 500f

        /**
         * 创建实例。使用 applicationContext 避免内存泄漏。
         */
        fun create(context: Context): LocationHelper =
            LocationHelper(context.applicationContext)

        /**
         * [修改点] 返回运行时需要申请的定位权限数组。
         *
         * - Android 12+（SDK 31）：同时申请 ACCESS_FINE_LOCATION + ACCESS_COARSE_LOCATION，
         *   系统会弹出"精确/粗略"位置选择弹窗。只申请 FINE 会导致部分 ROM 不回调定位。
         * - Android 11 及以下：仅申请 ACCESS_FINE_LOCATION（COARSE 隐含在 FINE 中）。
         */
        fun requiredLocationPermissions(): Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        /**
         * [修改点] 检查是否已获得任意定位权限（至少 COARSE）。
         * Android 12+：FINE 或 COARSE 任一授予即可；Android 11-：仅 FINE。
         */
        fun hasAnyLocationPermission(context: Context): Boolean {
            val fine = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fine || coarse
            } else {
                fine
            }
        }

        /**
         * [修改点] 检查是否已获得精确定位权限（FINE + COARSE 同时授予）。
         * Android 12+ 系统独立【精确位置】开关开启的表现。
         */
        fun hasFineLocationPermission(context: Context): Boolean =
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}
