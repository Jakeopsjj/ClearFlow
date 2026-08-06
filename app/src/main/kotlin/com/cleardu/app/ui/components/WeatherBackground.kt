package com.cleardu.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cleardu.app.data.weather.WeatherBackgroundManager
import com.cleardu.app.ui.theme.LiquidGlassColors
import android.graphics.BitmapFactory
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 多波纹扩散圆心数据
 */
private data class RippleCenter(
    val centerX: Float,
    val centerY: Float,
    val delayMs: Long,
    val durationMs: Int,
)

/**
 * Weather-aware background with multi-ripple diffusion transition.
 *
 * Layers (bottom to top):
 * 1. Local weather scene (Canvas drawn) — always visible
 * 2. Network image layer — clipped by expanding circles during transition
 * 3. Dark scrim for text readability
 * 4. Foreground content
 *
 * When a Pexels image loads:
 * - Random 1-3 circle centers are generated
 * - Each circle expands independently from 0 to full screen
 * - Inside circles: new image; outside: old local scene
 * - No blur, no opacity fade — both images stay sharp
 */
@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    background: Color = LiquidGlassColors.Background,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val weatherState by WeatherBackgroundManager.state.collectAsState()

    if (!weatherState.enabled) {
        MeshGradientBackground(modifier = modifier, background = background, content = content)
        return
    }

    val localType = weatherState.localBackgroundType
    val imageFile = weatherState.imageFile
    val hasImage = imageFile != null && imageFile.exists()

    var sizePx by remember { mutableStateOf(IntSize.Zero) }

    // Load bitmap from file (for Canvas drawing during transition)
    val imageBitmap = remember(imageFile?.absolutePath) {
        if (hasImage) {
            try {
                BitmapFactory.decodeFile(imageFile!!.absolutePath)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Generate ripple centers (stable per image change)
    val rippleCenters = remember(imageFile?.absolutePath) {
        if (hasImage) generateRippleCenters() else emptyList()
    }

    // Animation progress for each ripple (0f → 1f)
    val rippleProgressList = remember(imageFile?.absolutePath) {
        rippleCenters.map { Animatable(0f) }
    }

    // Trigger animation when image is available
    LaunchedEffect(imageFile) {
        if (hasImage && rippleProgressList.isNotEmpty()) {
            rippleProgressList.forEachIndexed { index, anim ->
                launch {
                    delay(rippleCenters[index].delayMs)
                    anim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = rippleCenters[index].durationMs,
                            easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
                        )
                    )
                }
            }
        }
    }

    val allAnimating = rippleProgressList.any { it.value < 1f }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { sizePx = it }
    ) {
        if (hasImage && imageBitmap != null && rippleProgressList.isNotEmpty() && sizePx != IntSize.Zero) {
            if (allAnimating) {
                // === Transition: draw local scene + new image clipped by expanding circles ===
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Draw local scene (outside circles)
                    with(WeatherSceneDrawer) { drawWeatherScene(localType) }

                    // 2. Build union of all ripples
                    val unionPath = Path()
                    rippleProgressList.forEachIndexed { index, anim ->
                        val center = rippleCenters[index]
                        val cx = center.centerX * size.width
                        val cy = center.centerY * size.height
                        val maxRadius = maxOf(
                            sqrt(cx.toDouble() * cx + cy.toDouble() * cy),
                            sqrt((size.width - cx).toDouble() * (size.width - cx) + cy.toDouble() * cy),
                            sqrt(cx.toDouble() * cx + (size.height - cy).toDouble() * (size.height - cy)),
                            sqrt((size.width - cx).toDouble() * (size.width - cx) + (size.height - cy).toDouble() * (size.height - cy))
                        ).toFloat() * 1.1f
                        val currentRadius = maxRadius * anim.value
                        unionPath.addOval(
                            Rect(
                                cx - currentRadius, cy - currentRadius,
                                cx + currentRadius, cy + currentRadius
                            )
                        )
                    }

                    // 3. Draw new image inside circles
                    clipPath(unionPath) {
                        drawImage(
                            image = imageBitmap,
                            dstSize = androidx.compose.ui.unit.IntSize(
                                size.width.toInt(),
                                size.height.toInt()
                            )
                        )
                        // Dark scrim
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Black.copy(alpha = 0.65f)
                                )
                            )
                        )
                    }
                }
            } else {
                // === Animation complete: show only new image ===
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .crossfade(false)
                        .build(),
                    contentDescription = "Weather background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Black.copy(alpha = 0.65f)
                                    )
                                )
                            )
                            drawContent()
                        }
                )
            }
        } else {
            // === No image: show local scene ===
            Canvas(modifier = Modifier.fillMaxSize()) {
                with(WeatherSceneDrawer) { drawWeatherScene(localType) }
            }
        }

        // Content layer
        content()
    }
}

/**
 * Generate 1-3 random ripple centers with different delays and durations.
 * Centers are spread across the screen, each with independent timing for a
 * natural, rhythmic expansion effect.
 */
private fun generateRippleCenters(): List<RippleCenter> {
    val count = Random.nextInt(1, 4) // 1 to 3
    val usedPositions = mutableListOf<Pair<Float, Float>>()

    return (0 until count).map { index ->
        var cx: Float
        var cy: Float
        var attempts = 0
        do {
            cx = Random.nextFloat() * 0.8f + 0.1f
            cy = Random.nextFloat() * 0.8f + 0.1f
            attempts++
        } while (attempts < 20 && usedPositions.any { (px, py) ->
            val dx = cx - px
            val dy = cy - py
            dx * dx + dy * dy < 0.15f * 0.15f
        })
        usedPositions.add(cx to cy)

        RippleCenter(
            centerX = cx,
            centerY = cy,
            delayMs = index * 200L + Random.nextLong(0, 400),
            durationMs = 2000 + Random.nextInt(0, 1500)
        )
    }
}