package com.cleardu.app.util

import com.cleardu.app.ui.components.NearbyHospital
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 通过 OpenStreetMap Overpass API 搜索附近医院。
 * 无需 API Key，免费使用。
 */
object NearbySearchService {

    private const val OVERPASS_URL = "https://overpass-api.de/api/interpreter"
    private const val SEARCH_RADIUS_METERS = 20_000 // 搜索半径 20km
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000

    /**
     * 搜索指定经纬度附近的医院。
     * @param lat 纬度
     * @param lng 经度
     * @return 附近医院列表（按距离排序）
     */
    suspend fun searchNearbyHospitals(lat: Double, lng: Double): List<NearbyHospital> =
        withContext(Dispatchers.IO) {
            val query = buildOverpassQuery(lat, lng)
            val response = executeQuery(query)
            parseHospitals(response, lat, lng)
        }

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

            // nodes 有直接 lat/lon；ways/relations 用 center
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

            // 构建地址
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

        // 按距离排序
        hospitals.sortBy { hospital ->
            calculateDistance(originLat, originLng, hospital.lat, hospital.lng)
        }

        return hospitals
    }

    /**
     * 使用 Haversine 公式计算两点间距离（米）。
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