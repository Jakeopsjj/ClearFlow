package com.cleardu.app.data.pexels

import com.google.gson.annotations.SerializedName

/**
 * Response from the Pexels proxy /api/search endpoint.
 */
data class PexelsSearchResponse(
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("photographer") val photographer: String = "",
    @SerializedName("photographerUrl") val photographerUrl: String = "",
    @SerializedName("alt") val alt: String = "",
    @SerializedName("error") val error: String? = null
)
