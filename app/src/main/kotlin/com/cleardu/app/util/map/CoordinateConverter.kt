package com.cleardu.app.util.map

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 坐标系转换工具：BD-09 ↔ GCJ-02 ↔ WGS-84
 *
 * - GCJ-02：国测局坐标系，高德、腾讯地图使用
 * - BD-09：百度坐标系，百度地图使用
 * - WGS-84：GPS 原始坐标系，系统原生定位使用
 */
object CoordinateConverter {

    private const val X_PI = PI * 3000.0 / 180.0
    private const val A = 6378245.0  // 长半轴
    private const val EE = 0.00669342162296594323  // 扁率

    // ==================== WGS-84 → GCJ-02 ====================

    fun wgs84ToGcj02(lat: Double, lng: Double): Pair<Double, Double> {
        if (outOfChina(lat, lng)) return lat to lng
        var dLat = transformLat(lng - 105.0, lat - 35.0)
        var dLng = transformLng(lng - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * PI
        var magic = sin(radLat)
        magic = 1 - EE * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
        dLng = (dLng * 180.0) / (A / sqrtMagic * cos(radLat) * PI)
        return (lat + dLat) to (lng + dLng)
    }

    // ==================== GCJ-02 → WGS-84 ====================

    fun gcj02ToWgs84(lat: Double, lng: Double): Pair<Double, Double> {
        if (outOfChina(lat, lng)) return lat to lng
        val (dLat, dLng) = wgs84ToGcj02(lat, lng)
        return (lat * 2 - dLat) to (lng * 2 - dLng)
    }

    // ==================== GCJ-02 → BD-09 ====================

    fun gcj02ToBd09(lat: Double, lng: Double): Pair<Double, Double> {
        val x = lng
        val y = lat
        val z = sqrt(x * x + y * y) + 0.00002 * sin(y * X_PI)
        val theta = kotlin.math.atan2(y, x) + 0.000003 * cos(x * X_PI)
        val bdLng = z * cos(theta) + 0.0065
        val bdLat = z * sin(theta) + 0.006
        return bdLat to bdLng
    }

    // ==================== BD-09 → GCJ-02 ====================

    fun bd09ToGcj02(lat: Double, lng: Double): Pair<Double, Double> {
        val x = lng - 0.0065
        val y = lat - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * X_PI)
        val theta = kotlin.math.atan2(y, x) - 0.000003 * cos(x * X_PI)
        return (z * sin(theta)) to (z * cos(theta))
    }

    // ==================== WGS-84 → BD-09 ====================

    fun wgs84ToBd09(lat: Double, lng: Double): Pair<Double, Double> {
        val (gcjLat, gcjLng) = wgs84ToGcj02(lat, lng)
        return gcj02ToBd09(gcjLat, gcjLng)
    }

    // ==================== BD-09 → WGS-84 ====================

    fun bd09ToWgs84(lat: Double, lng: Double): Pair<Double, Double> {
        val (gcjLat, gcjLng) = bd09ToGcj02(lat, lng)
        return gcj02ToWgs84(gcjLat, gcjLng)
    }

    // ==================== 内部工具 ====================

    private fun outOfChina(lat: Double, lng: Double): Boolean =
        lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271

    private fun transformLat(x: Double, y: Double): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(y * PI) + 40.0 * sin(y / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * sin(y / 12.0 * PI) + 320.0 * sin(y * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLng(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(x * PI) + 40.0 * sin(x / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * sin(x / 12.0 * PI) + 300.0 * sin(x / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

    /**
     * Haversine 公式计算两点间距离（米）
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * kotlin.math.atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}