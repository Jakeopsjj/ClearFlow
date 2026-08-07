package com.cleardu.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cleardu.app.R
import com.cleardu.app.data.weather.WeatherBackgroundManager
import com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType
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
 * 将本地天气类型映射到内置 drawable 资源 ID。
 * 每种天气类型对应一张 4K 全屏真实天气素材图片。
 */
fun getLocalWeatherDrawable(type: LocalBackgroundType): Int = when (type) {
    LocalBackgroundType.SUNNY_DAY,
    LocalBackgroundType.SUNNY_NIGHT -> R.drawable.bg_weather_sunny
    LocalBackgroundType.CLOUDY_DAY,
    LocalBackgroundType.CLOUDY_NIGHT -> R.drawable.bg_weather_cloudy
    LocalBackgroundType.OVERCAST -> R.drawable.bg_weather_overcast
    LocalBackgroundType.RAIN_DAY,
    LocalBackgroundType.RAIN_NIGHT -> R.drawable.bg_weather_rain
    LocalBackgroundType.HEAVY_RAIN_DAY,
    LocalBackgroundType.HEAVY_RAIN_NIGHT -> R.drawable.bg_weather_heavy_rain
    LocalBackgroundType.STORM -> R.drawable.bg_weather_storm
    LocalBackgroundType.SNOW_DAY,
    LocalBackgroundType.SNOW_NIGHT -> R.drawable.bg_weather_snow
    LocalBackgroundType.FOG -> R.drawable.bg_weather_overcast
    LocalBackgroundType.SANDSTORM -> R.drawable.bg_weather_overcast
}

/**
 * 天气感知背景组件，带多波纹扩散替换动画。
 *
 * 放在 App 根层级（MainActivity 中包裹 NavHost），确保跨页面切换时背景不重新加载。
 *
 * 层级（从底到顶）：
 * 1. 本地内置天气背景（真实 4K 图片素材）
 * 2. 网络 Pexels 天气背景图 — 过渡期间由扩展圆形裁剪，动画完成后完整显示
 * 3. 前景内容
 *
 * 多波纹扩散动画：
 * - 仅当天气编码发生真实变更时触发（isWeatherCodeChanged = true）
 * - 随机生成 1-3 个圆形圆心
 * - 每个圆形独立缓慢向外扩张
 * - 圆形内部显示新天气背景图，外部保留旧天气背景图
 * - 两张图均保持清晰，不做模糊/透明度淡入淡出
 * - 天气无变化时直接显示当前背景，不触发动画
 * - 动画完成后触发亮度联动 UI 更新（通过 backgroundAwareColors 自动响应状态变化）
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
    val previousLocalType = weatherState.previousLocalBackgroundType
    val isWeatherCodeChanged = weatherState.isWeatherCodeChanged
    val imageFile = weatherState.imageFile
    val hasImage = imageFile != null && imageFile.exists()

    var sizePx by remember { mutableStateOf(IntSize.Zero) }

    // 本地内置背景 drawable 资源 ID
    val localDrawableRes = remember(localType) { getLocalWeatherDrawable(localType) }
    val previousLocalDrawableRes = remember(previousLocalType) { getLocalWeatherDrawable(previousLocalType) }

    // 从网络图片文件加载 Bitmap（用于无天气变更时的直接显示）
    val imageBitmap = remember(imageFile?.absolutePath) {
        if (hasImage) {
            try {
                BitmapFactory.decodeFile(imageFile!!.absolutePath)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // 当天气变更时生成波纹圆心
    val rippleCenters = remember(isWeatherCodeChanged, localType) {
        if (isWeatherCodeChanged) generateRippleCenters() else emptyList()
    }

    // 每个波纹的动画进度（0f → 1f）
    val rippleProgressList = remember(isWeatherCodeChanged, localType) {
        rippleCenters.map { Animatable(0f) }
    }

    // 当天气变更时触发动画
    LaunchedEffect(isWeatherCodeChanged, localType) {
        if (isWeatherCodeChanged && rippleProgressList.isNotEmpty()) {
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
            // 动画完成后重置天气变更标志
            WeatherBackgroundManager.onWeatherCodeAnimationComplete()
        }
    }

    val allAnimating = rippleProgressList.isNotEmpty() && rippleProgressList.any { it.value < 1f }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { sizePx = it }
    ) {
        if (isWeatherCodeChanged && rippleProgressList.isNotEmpty() && sizePx != IntSize.Zero) {
            // === 天气变更过渡动画：旧背景 + 新背景通过扩展圆形裁剪 ===
            val oldPainter = painterResource(previousLocalDrawableRes)
            val newPainter = painterResource(localDrawableRes)

            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. 绘制旧天气背景图（圆形外部）
                with(oldPainter) {
                    draw(size = this@Canvas.size)
                }

                // 2. 构建所有波纹的并集路径
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

                // 3. 在圆形内部绘制新天气背景图
                clipPath(unionPath) {
                    with(newPainter) {
                        draw(size = this@Canvas.size)
                    }
                }
            }
        } else if (isWeatherCodeChanged && rippleProgressList.isEmpty()) {
            // 天气变更但波纹未生成（极端情况），直接显示新本地背景
            Image(
                painter = painterResource(localDrawableRes),
                contentDescription = "Local weather background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (hasImage && imageBitmap != null) {
            // === 无天气变更：显示网络 Pexels 图片 ===
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageFile)
                    .crossfade(false)
                    .build(),
                contentDescription = "Weather background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // === 无天气变更且无网络图片：显示本地内置背景 ===
            Image(
                painter = painterResource(localDrawableRes),
                contentDescription = "Local weather background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 前景内容层
        content()
    }
}

/**
 * 生成 1-3 个随机波纹圆心，各自独立延迟和持续时间。
 * 圆心分布在整个屏幕范围内，每个圆心有独立的节奏，实现自然扩散效果。
 */
private fun generateRippleCenters(): List<RippleCenter> {
    val count = Random.nextInt(1, 4) // 1 到 3
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