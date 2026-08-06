package com.cleardu.app.data.weather

import com.google.gson.annotations.SerializedName

/**
 * Location search response from weathercn.com geoposition API.
 */
data class LocationResponse(
    @SerializedName("Key") val key: String = "",
    @SerializedName("LocalizedName") val localizedName: String = "",
    @SerializedName("Country") val country: Country? = null
) {
    data class Country(
        @SerializedName("ID") val id: String = "",
        @SerializedName("LocalizedName") val localizedName: String = ""
    )
}

/**
 * Current conditions response (array, take first element).
 */
data class CurrentConditions(
    @SerializedName("LocalObservationDateTime") val localObservationDateTime: String = "",
    @SerializedName("EpochTime") val epochTime: Long = 0L,
    @SerializedName("WeatherText") val weatherText: String = "",
    @SerializedName("WeatherIcon") val weatherIcon: Int = 0,
    @SerializedName("IsDayTime") val isDayTime: Boolean = true,
    @SerializedName("Temperature") val temperature: Temperature? = null,
    @SerializedName("LocalSource") val localSource: LocalSource? = null,
    @SerializedName("RelativeHumidity") val relativeHumidity: Int = 0,
    @SerializedName("UVIndex") val uvIndex: Int = 0
) {
    data class Temperature(
        @SerializedName("Metric") val metric: Unit? = null,
        @SerializedName("Imperial") val imperial: Unit? = null
    ) {
        data class Unit(
            @SerializedName("Value") val value: Double = 0.0,
            @SerializedName("Unit") val unit: String = "",
            @SerializedName("UnitType") val unitType: Int = 0
        )
    }

    data class LocalSource(
        @SerializedName("Id") val id: Int = 0,
        @SerializedName("Name") val name: String = "",
        @SerializedName("WeatherCode") val weatherCode: String = "00"
    )
}

/**
 * Simplified weather data used by the app.
 */
data class WeatherInfo(
    val weatherCode: String,        // 华风爱科 WeatherCode (e.g. "00" for sunny)
    val weatherText: String,        // 天气描述 (e.g. "晴")
    val isDayTime: Boolean,         // true=白天, false=夜间
    val temperatureC: Double,       // 温度 (摄氏度)
    val humidity: Int,              // 相对湿度 (%)
    val uvIndex: Int,               // 紫外线指数
    val timestamp: Long             // 数据获取时间 (epoch millis)
)
