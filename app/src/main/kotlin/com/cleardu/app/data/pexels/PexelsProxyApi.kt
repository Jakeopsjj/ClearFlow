package com.cleardu.app.data.pexels

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Pexels proxy server.
 * The proxy holds the Pexels API key; the app does not need it.
 */
interface PexelsProxyApi {

    /**
     * Search for a landscape photo matching the query.
     * @param query English search keyword (e.g. "sunny sky landscape")
     * @param orientation "landscape" (default)
     * @return PexelsSearchResponse with imageUrl or error
     */
    @GET("api/search")
    suspend fun searchPhoto(
        @Query("query") query: String,
        @Query("orientation") orientation: String = "landscape"
    ): PexelsSearchResponse?
}
