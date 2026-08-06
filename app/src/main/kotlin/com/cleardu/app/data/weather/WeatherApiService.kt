package com.cleardu.app.data.weather

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for 华风爱科 (weathercn.com) API.
 *
 * Flow:
 * 1. Search location by lat/lon → get locationKey
 * 2. Get current conditions by locationKey → get WeatherCode + temperature
 */
interface WeatherApiService {

    /**
     * Search location by geographic coordinates.
     * @param apikey API key
     * @param q comma-separated "lat,lon" (e.g. "39.921,116.469")
     * @return LocationResponse with Key field
     */
    @GET("locations/v1/cities/geoposition/search")
    suspend fun searchLocation(
        @Query("apikey") apikey: String,
        @Query("q") q: String,
        @Query("language") language: String = "zh-cn"
    ): LocationResponse?

    /**
     * Get current weather conditions for a location.
     * @param locationKey location key from searchLocation
     * @param apikey API key
     * @return list of current conditions (take first)
     */
    @GET("currentconditions/v1/{locationKey}")
    suspend fun getCurrentConditions(
        @Path("locationKey") locationKey: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String = "zh-cn",
        @Query("details") details: Boolean = true
    ): List<CurrentConditions>?
}
