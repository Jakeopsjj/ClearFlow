package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cleardu.app.data.weather.WeatherBackgroundManager
import com.cleardu.app.data.weather.WeatherCodeMapper
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Weather-aware background — drop-in replacement for [MeshGradientBackground].
 *
 * Behavior:
 * - When weather background is **disabled** (default): delegates to [MeshGradientBackground].
 * - When **enabled** with a cached Pexels image: shows the image with fade-in + dark scrim.
 * - When **enabled** but no image (network failed): shows a Compose-drawn gradient
 *   matched to the current weather code + day/night.
 *
 * All screens that previously used [MeshGradientBackground] can switch to this
 * composable with no other changes. The [WeatherBackgroundManager] singleton
 * provides the state; no parameters need to be passed.
 *
 * @param modifier outer layout modifier
 * @param background substrate color (used when weather is disabled)
 * @param content foreground content drawn on top
 */
@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    background: Color = LiquidGlassColors.Background,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val weatherState by WeatherBackgroundManager.state.collectAsState()

    if (!weatherState.enabled) {
        // Weather background disabled — use original mesh gradient
        MeshGradientBackground(modifier = modifier, background = background, content = content)
        return
    }

    // Weather background enabled
    val imageFile = weatherState.imageFile
    val localType = weatherState.localBackgroundType

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Always draw the local gradient as a base layer.
                // If an image loads on top, it covers this; if not, this is the visible background.
                drawLocalWeatherGradient(localType)
            }
    ) {
        // Image layer (if available)
        if (imageFile != null && imageFile.exists()) {
            val imageAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800),
                label = "weatherImageFade"
            )

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageFile)
                    .crossfade(false) // we handle fade ourselves
                    .build(),
                contentDescription = "Weather background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(imageAlpha)
            )

            // Dark scrim overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
                    .alpha(imageAlpha)
            )
        }

        // Content layer on top of background
        content()
    }
}

/**
 * Draw a gradient background based on the local weather type.
 * Used as fallback when no Pexels image is available.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLocalWeatherGradient(
    type: WeatherCodeMapper.LocalBackgroundType
) {
    val (topColor, bottomColor) = when (type) {
        WeatherCodeMapper.LocalBackgroundType.SUNNY_DAY ->
            Color(0xFF1565C0) to Color(0xFF42A5F5)
        WeatherCodeMapper.LocalBackgroundType.SUNNY_NIGHT ->
            Color(0xFF0A0E27) to Color(0xFF1A237E)
        WeatherCodeMapper.LocalBackgroundType.CLOUDY_DAY ->
            Color(0xFF455A64) to Color(0xFF90A4AE)
        WeatherCodeMapper.LocalBackgroundType.CLOUDY_NIGHT ->
            Color(0xFF1C1C2E) to Color(0xFF263238)
        WeatherCodeMapper.LocalBackgroundType.OVERCAST ->
            Color(0xFF37474F) to Color(0xFF607D8B)
        WeatherCodeMapper.LocalBackgroundType.RAIN_DAY ->
            Color(0xFF263238) to Color(0xFF546E7A)
        WeatherCodeMapper.LocalBackgroundType.RAIN_NIGHT ->
            Color(0xFF0D1B2A) to Color(0xFF1B2838)
        WeatherCodeMapper.LocalBackgroundType.SNOW_DAY ->
            Color(0xFF78909C) to Color(0xFFB0BEC5)
        WeatherCodeMapper.LocalBackgroundType.SNOW_NIGHT ->
            Color(0xFF1A237E) to Color(0xFF283593)
        WeatherCodeMapper.LocalBackgroundType.STORM ->
            Color(0xFF1A0033) to Color(0xFF311B92)
        WeatherCodeMapper.LocalBackgroundType.FOG ->
            Color(0xFF546E7A) to Color(0xFF90A4AE)
        WeatherCodeMapper.LocalBackgroundType.SANDSTORM ->
            Color(0xFF4E342E) to Color(0xFF8D6E63)
    }

    // Base vertical gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(topColor, bottomColor),
            startY = 0f,
            endY = size.height
        )
    )

    // Add subtle radial highlights for depth (similar to mesh gradient)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = androidx.compose.ui.geometry.Offset(
                size.width * 0.2f,
                size.height * 0.3f
            ),
            radius = size.minDimension * 0.6f
        )
    )

    // Dark overlay at bottom for text readability
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.3f)
            ),
            startY = size.height * 0.5f,
            endY = size.height
        )
    )
}
