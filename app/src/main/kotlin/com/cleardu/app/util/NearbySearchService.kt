package com.cleardu.app.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.cleardu.app.ui.components.NearbyHospital
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * 搜索附近医院。
 *
 * 优先使用 Android 系统内置 Geocoder——国产 ROM（HyperOS/ColorOS/OriginOS/
 * MagicOS/realmeUI）底层被厂商替换为百度/高德服务，能搜到高质量医院数据。
 * 如果 Geocoder 不可用或无结果，回退到 Overpass API（OpenStreetMap）。
 */
object NearbySearchService {

    private const val OVERPASS_URL = "https://overpass-api.de/api/interpreter"
    private const val SEARCH_RADIUS_METERS = 20_000
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000

    /** 搜索边界框的半边长（度），约 5km */
    private const val GEOCODER_DELTA = 0.05

    /**
     * 搜索指定经纬度附近的医院。
     *
     * @param context Android Context（用于 Geocoder）
     * @param lat 纬度
     * @param lng 经度
     * @return 附近医院列表（按距离排序）
     */
    suspend fun searchNearbyHospitals(context: Context, lat: Double, lng: Double): List<NearbyHospital> =
        withContext(Dispatchers.IO) {
            // 1) 优先用 Android Geocoder（国产 ROM 底层是百度/高德）
            val geocoderResults = searchViaGeocoder(context, lat, lng)
            if (geocoderResults.isNotEmpty()) return@withContext geocoderResults

            // 2) 回退到 Overpass API
            try {
                val query = buildOverpassQuery(lat, lng)
                val response = executeQuery(query)
                parseHospitals(response, lat, lng)
            } catch (_: Exception) {
                emptyList()
            }
        }

    /**
     * 通过 Android Geocoder 搜索附近医院。
     * 国产 ROM 上 Geocoder 底层使用百度/高德服务，数据质量高。
     */
    private fun searchViaGeocoder(context: Context, lat: Double, lng: Double): List<NearbyHospital> {
        if (!Geocoder.isPresent()) return emptyList()

        val geocoder = Geocoder(context, Locale.CHINA)
        val lowerLat = lat - GEOCODER_DELTA
        val lowerLng = lng - GEOCODER_DELTA
        val upperLat = lat + GEOCODER_DELTA
        val upperLng = lng + GEOCODER_DELTA

        val allResults = mutableListOf<Address>()
        try {
            // 搜索多种关键词，覆盖不同类型的医院
            for (keyword in listOf("医院", "人民医院", "中医院", "中心医院", "附属医院")) {
                try {
                    val results = geocoder.getFromLocationName(
                        keyword, 20,
                        lowerLat, lowerLng, upperLat, upperLng
                    )
                    if (results != null) {
                        allResults.addAll(results)
                    }
                } catch (_: Exception) {
                    // 某些关键词可能搜不到，忽略
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }

        if (allResults.isEmpty()) return emptyList()

        // 去重（按名称）
        val seen = mutableSetOf<String>()
        val hospitals = allResults
            .filter { addr ->
                val name = addr.featureName ?: addr.getAddressLine(0) ?: ""
                name.isNotBlank() && seen.add(name)
            }
            .mapNotNull { addr ->
                val name = addr.featureName?.takeIf { it.isNotBlank() }
                    ?: addr.getAddressLine(0)?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null

                // 过滤明显不是医院的结果
                if (name.length < 2 || name.length > 30) return@mapNotNull null

                val hLat = addr.latitude
                val hLng = addr.longitude
                if (hLat == 0.0 && hLng == 0.0) return@mapNotNull null

                val address = addr.getAddressLine(0) ?: ""
                val distance = calculateDistance(lat, lng, hLat, hLng)

                NearbyHospital(
                    name = name,
                    address = address.ifBlank { "未知地址" },
                    distance = formatDistance(distance),
                    lat = hLat,
                    lng = hLng
                )
            }
            .sortedBy { calculateDistance(lat, lng, it.lat, it.lng) }

        return hospitals.take(30)
    }

    // ==================== Overpass API 回退 ====================

    private fun buildOverpassQuery(lat: Double, lng: Double): String {
        return """
            [out:json][timeout:15];
            (
              node["amenity"="hospital"](around:$SEARCH_RADIUS_METERS,$lat,$lng);
              way["amenity"="hospital"](around:$SEARCH_RADIUS_METERS,$lat,$lng);
              relation["amenity"="hospital"](around:$SEARCH_RADIUS_METERS,$lat,$lng);
            );
            out center 50;
        """.trimIndent()
    }

    private fun executeQuery(query: String): String {
        val url = URL(OVERPASS_URL)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("User-Agent", "ClearDu/1.0")

            connection.outputStream.use { os ->
                os.write("data=$query".toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            return if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            } else {
                throw Exception("Overpass API returned HTTP $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseHospitals(json: String, originLat: Double, originLng: Double): List<NearbyHospital> {
        val root = JSONObject(json)
        val elements: JSONArray = root.optJSONArray("elements") ?: return emptyList()

        val hospitals = mutableListOf<NearbyHospital>()
        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            val tags = element.optJSONObject("tags") ?: continue
            val name = tags.optString("name", "")
            if (name.isBlank()) continue

            val hLat: Double
            val hLng: Double
            if (element.optString("type") == "node") {
                hLat = element.optDouble("lat", Double.NaN)
                hLng = element.optDouble("lon", Double.NaN)
            } else {
                val center = element.optJSONObject("center")
                if (center != null) {
                    hLat = center.optDouble("lat", Double.NaN)
                    hLng = center.optDouble("lon", Double.NaN)
                } else {
                    continue
                }
            }

            if (hLat.isNaN() || hLng.isNaN()) continue

            val street = tags.optString("addr:street", "")
            val city = tags.optString("addr:city", "")
            val address = listOf(street, city).filter { it.isNotBlank() }.joinToString(", ")

            val distance = calculateDistance(originLat, originLng, hLat, hLng)

            hospitals.add(
                NearbyHospital(
                    name = name,
                    address = address.ifBlank { "未知地址" },
                    distance = formatDistance(distance),
                    lat = hLat,
                    lng = hLng
                )
            )
        }

        hospitals.sortBy { calculateDistance(originLat, originLng, it.lat, it.lng) }
        return hospitals
    }

    /**
     * Haversine 公式计算两点间距离（米）。
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun formatDistance(meters: Double): String {
        return when {
            meters < 1000 -> "${meters.toInt()}m"
            else -> "${"%.1f".format(meters / 1000)}km"
        }
    }
}