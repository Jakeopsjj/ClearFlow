package com.cleardu.app.data.weather

import java.util.Calendar

/**
 * Maps 华风爱科 WeatherCode to:
 * - English search keywords for Pexels (day/night variants)
 * - Local fallback background type
 *
 * WeatherCode table: http://apidoc.weathercn.com/developers/best-practices.html
 */
object WeatherCodeMapper {

    /** Local background types mapped to built-in 4K real weather photo resources. */
    enum class LocalBackgroundType {
        SUNNY_DAY,
        SUNNY_NIGHT,
        CLOUDY_DAY,
        CLOUDY_NIGHT,
        OVERCAST,
        RAIN_DAY,
        RAIN_NIGHT,
        HEAVY_RAIN_DAY,
        HEAVY_RAIN_NIGHT,
        SNOW_DAY,
        SNOW_NIGHT,
        STORM,
        FOG,
        SANDSTORM
    }

    /**
     * Get the Chinese name for a weather code.
     */
    fun getChineseName(code: String): String = when (code) {
        "00" -> "晴"
        "01" -> "多云"
        "02" -> "阴"
        "03" -> "阵雨"
        "04" -> "雷阵雨"
        "05" -> "雷阵雨伴有冰雹"
        "06" -> "雨夹雪"
        "07" -> "小雨"
        "08" -> "中雨"
        "09" -> "大雨"
        "10" -> "暴雨"
        "11" -> "大暴雨"
        "12" -> "特大暴雨"
        "13" -> "阵雪"
        "14" -> "小雪"
        "15" -> "中雪"
        "16" -> "大雪"
        "17" -> "暴雪"
        "18" -> "雾"
        "19" -> "冻雨"
        "20" -> "沙尘暴"
        "21" -> "小到中雨"
        "22" -> "中到大雨"
        "23" -> "大到暴雨"
        "24" -> "暴雨到大暴雨"
        "25" -> "大暴雨到特大暴雨"
        "26" -> "小到中雪"
        "27" -> "中到大雪"
        "28" -> "大到暴雪"
        "29" -> "浮尘"
        "30" -> "扬沙"
        "31" -> "强沙尘暴"
        "32" -> "霾"
        else -> "未知"
    }

    /**
     * Generate an English search keyword for Pexels based on weather code
     * and day/night status.
     *
     * The keyword is used to search for landscape background images on Pexels.
     */
    fun getPexelsSearchKeyword(weatherCode: String, isDayTime: Boolean): String {
        val dayNight = if (isDayTime) "" else "night "

        return when (weatherCode) {
            // Sunny
            "00" -> if (isDayTime) "sunny sky landscape" else "starry night sky landscape"
            // Cloudy
            "01" -> if (isDayTime) "partly cloudy sky landscape" else "cloudy night sky landscape"
            // Overcast
            "02" -> "overcast sky landscape"
            // Shower
            "03" -> if (isDayTime) "rain shower landscape" else "night rain landscape"
            // Thundershower
            "04", "05" -> if (isDayTime) "thunderstorm landscape" else "night thunderstorm landscape"
            // Sleet
            "06" -> "sleet landscape"
            // Light rain
            "07" -> if (isDayTime) "light rain landscape" else "night light rain landscape"
            // Moderate rain
            "08" -> if (isDayTime) "rain landscape" else "night rain landscape"
            // Heavy rain
            "09" -> if (isDayTime) "heavy rain landscape" else "night heavy rain landscape"
            // Storm / Heavy storm / Severe storm
            "10", "11", "12", "24", "25" -> if (isDayTime) "storm rain landscape" else "night storm landscape"
            // Rain ranges
            "21", "22", "23" -> if (isDayTime) "rain landscape" else "night rain landscape"
            // Snow shower
            "13" -> if (isDayTime) "snow landscape" else "night snow landscape"
            // Light snow
            "14" -> if (isDayTime) "light snow landscape" else "night light snow landscape"
            // Moderate snow
            "15" -> if (isDayTime) "snowfall landscape" else "night snowfall landscape"
            // Heavy snow / Snowstorm
            "16", "17", "28" -> if (isDayTime) "heavy snow landscape" else "night heavy snow landscape"
            // Snow ranges
            "26", "27" -> if (isDayTime) "snow landscape" else "night snow landscape"
            // Fog
            "18" -> "foggy forest landscape"
            // Freezing rain
            "19" -> "freezing rain landscape"
            // Sandstorm
            "20", "31" -> "sandstorm desert landscape"
            // Dust / Sand
            "29", "30" -> "dusty desert landscape"
            // Haze
            "32" -> "hazy city landscape"
            // Default
            else -> if (isDayTime) "nature landscape" else "night landscape"
        }
    }

    /**
     * Get the local fallback background type for a weather code and day/night status.
     * Used when all proxy services are unavailable.
     */
    fun getLocalBackgroundType(weatherCode: String, isDayTime: Boolean): LocalBackgroundType {
        return when (weatherCode) {
            "00" -> if (isDayTime) LocalBackgroundType.SUNNY_DAY else LocalBackgroundType.SUNNY_NIGHT
            "01" -> if (isDayTime) LocalBackgroundType.CLOUDY_DAY else LocalBackgroundType.CLOUDY_NIGHT
            "02" -> LocalBackgroundType.OVERCAST
            "03", "07", "08", "21", "22", "23" ->
                if (isDayTime) LocalBackgroundType.RAIN_DAY else LocalBackgroundType.RAIN_NIGHT
            "09" -> if (isDayTime) LocalBackgroundType.HEAVY_RAIN_DAY else LocalBackgroundType.HEAVY_RAIN_NIGHT
            "04", "05", "10", "11", "12", "24", "25" -> LocalBackgroundType.STORM
            "06", "13", "14", "15", "16", "17", "26", "27", "28" ->
                if (isDayTime) LocalBackgroundType.SNOW_DAY else LocalBackgroundType.SNOW_NIGHT
            "18", "32" -> LocalBackgroundType.FOG
            "19" -> if (isDayTime) LocalBackgroundType.RAIN_DAY else LocalBackgroundType.RAIN_NIGHT
            "20", "29", "30", "31" -> LocalBackgroundType.SANDSTORM
            else -> if (isDayTime) LocalBackgroundType.SUNNY_DAY else LocalBackgroundType.SUNNY_NIGHT
        }
    }

    /**
     * Determine if it's currently daytime based on system clock.
     * Daytime: 06:00 - 18:00, Nighttime: 18:00 - 06:00
     */
    fun isDayTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 6..17
    }
}
