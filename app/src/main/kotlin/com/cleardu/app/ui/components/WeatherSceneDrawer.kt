package com.cleardu.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Draws detailed weather scenes as local fallback backgrounds.
 * Each weather type produces a rich, gradient-based scene using Canvas primitives.
 */
object WeatherSceneDrawer {

    /** Draws the complete weather scene matching [type]. */
    fun DrawScope.drawWeatherScene(type: com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType) {
        when (type) {
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.SUNNY_DAY -> drawSunnyDay()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.SUNNY_NIGHT -> drawSunnyNight()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.CLOUDY_DAY -> drawCloudyDay()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.CLOUDY_NIGHT -> drawCloudyNight()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.OVERCAST -> drawOvercast()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.RAIN_DAY -> drawRainDay()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.RAIN_NIGHT -> drawRainNight()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.SNOW_DAY -> drawSnowDay()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.SNOW_NIGHT -> drawSnowNight()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.STORM -> drawStorm()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.FOG -> drawFog()
            com.cleardu.app.data.weather.WeatherCodeMapper.LocalBackgroundType.SANDSTORM -> drawSandstorm()
        }
    }

    // ===== Sunny Day =====
    private fun DrawScope.drawSunnyDay() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF81D4FA)),
            startY = 0f, endY = size.height
        ))
        drawSun(0.75f, 0.18f, size.minDimension * 0.12f)
        drawSunRays()
        drawScatteredCloud(0.10f, 0.22f, 0.10f)
        drawScatteredCloud(0.65f, 0.35f, 0.08f)
        drawScatteredCloud(0.90f, 0.15f, 0.07f)
        drawGroundGradient()
    }

    // ===== Sunny Night =====
    private fun DrawScope.drawSunnyNight() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF0A0E27), Color(0xFF1A237E), Color(0xFF283593)),
            startY = 0f, endY = size.height
        ))
        drawStars(30)
        drawMoon(0.75f, 0.15f, size.minDimension * 0.10f)
        drawGroundGradient()
    }

    // ===== Cloudy Day =====
    private fun DrawScope.drawCloudyDay() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF455A64), Color(0xFF78909C), Color(0xFF90A4AE)),
            startY = 0f, endY = size.height
        ))
        drawScatteredCloud(0.15f, 0.15f, 0.12f)
        drawScatteredCloud(0.50f, 0.25f, 0.14f)
        drawScatteredCloud(0.75f, 0.12f, 0.11f)
        drawScatteredCloud(0.30f, 0.45f, 0.09f)
        drawScatteredCloud(0.85f, 0.40f, 0.10f)
        drawGroundGradient()
    }

    // ===== Cloudy Night =====
    private fun DrawScope.drawCloudyNight() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF1C1C2E), Color(0xFF263238), Color(0xFF37474F)),
            startY = 0f, endY = size.height
        ))
        drawStars(12)
        drawScatteredCloud(0.15f, 0.15f, 0.13f, alpha = 0.7f)
        drawScatteredCloud(0.55f, 0.22f, 0.15f, alpha = 0.7f)
        drawScatteredCloud(0.80f, 0.10f, 0.12f, alpha = 0.7f)
        drawScatteredCloud(0.35f, 0.50f, 0.10f, alpha = 0.6f)
        drawGroundGradient()
    }

    // ===== Overcast =====
    private fun DrawScope.drawOvercast() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF37474F), Color(0xFF546E7A), Color(0xFF607D8B)),
            startY = 0f, endY = size.height
        ))
        drawOvercastCloud(0.10f, 0.08f, 0.40f, 0.25f)
        drawOvercastCloud(0.50f, 0.20f, 0.45f, 0.28f)
        drawOvercastCloud(0.05f, 0.50f, 0.50f, 0.22f)
        drawOvercastCloud(0.55f, 0.55f, 0.42f, 0.20f)
        drawGroundGradient()
    }

    // ===== Rain Day =====
    private fun DrawScope.drawRainDay() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF263238), Color(0xFF455A64), Color(0xFF546E7A)),
            startY = 0f, endY = size.height
        ))
        drawOvercastCloud(0.10f, 0.08f, 0.40f, 0.25f)
        drawOvercastCloud(0.50f, 0.15f, 0.45f, 0.28f)
        drawRain(60)
        drawGroundGradient()
    }

    // ===== Rain Night =====
    private fun DrawScope.drawRainNight() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF0D1B2A), Color(0xFF1B2838), Color(0xFF263238)),
            startY = 0f, endY = size.height
        ))
        drawOvercastCloud(0.10f, 0.08f, 0.40f, 0.25f, alpha = 0.6f)
        drawOvercastCloud(0.50f, 0.15f, 0.45f, 0.28f, alpha = 0.6f)
        drawRain(50)
        drawGroundGradient()
    }

    // ===== Snow Day =====
    private fun DrawScope.drawSnowDay() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF78909C), Color(0xFF90A4AE), Color(0xFFB0BEC5)),
            startY = 0f, endY = size.height
        ))
        drawOvercastCloud(0.10f, 0.08f, 0.40f, 0.25f, alpha = 0.8f)
        drawSnow(40)
        drawGroundGradient(snowGround = true)
    }

    // ===== Snow Night =====
    private fun DrawScope.drawSnowNight() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB)),
            startY = 0f, endY = size.height
        ))
        drawMoon(0.80f, 0.12f, size.minDimension * 0.07f, alpha = 0.5f)
        drawSnow(35)
        drawGroundGradient(snowGround = true)
    }

    // ===== Storm =====
    private fun DrawScope.drawStorm() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF1A0033), Color(0xFF311B92), Color(0xFF4527A0)),
            startY = 0f, endY = size.height
        ))
        drawOvercastCloud(0.10f, 0.08f, 0.45f, 0.30f, alpha = 0.85f)
        drawOvercastCloud(0.45f, 0.18f, 0.50f, 0.33f, alpha = 0.85f)
        drawLightning(0.30f, 0.35f)
        drawLightning(0.70f, 0.28f)
        drawRain(40)
        drawGroundGradient()
    }

    // ===== Fog =====
    private fun DrawScope.drawFog() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF546E7A), Color(0xFF78909C), Color(0xFF90A4AE)),
            startY = 0f, endY = size.height
        ))
        drawFogLayer(0.15f, 0.30f)
        drawFogLayer(0.45f, 0.28f)
        drawFogLayer(0.70f, 0.25f)
        drawGroundGradient()
    }

    // ===== Sandstorm =====
    private fun DrawScope.drawSandstorm() {
        drawRect(Brush.verticalGradient(
            listOf(Color(0xFF4E342E), Color(0xFF6D4C41), Color(0xFF8D6E63)),
            startY = 0f, endY = size.height
        ))
        drawSandParticles(40)
        drawGroundGradient()
    }

    // ==================== Drawing Primitives ====================

    private fun DrawScope.drawSun(cx: Float, cy: Float, radius: Float) {
        val x = size.width * cx
        val y = size.height * cy
        // Outer glow
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0x40FFFFFF), Color.Transparent),
                center = Offset(x, y),
                radius = radius * 3f
            ),
            radius = radius * 3f, center = Offset(x, y)
        )
        // Sun body
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFFFF9C4), Color(0xFFFFE082), Color(0xFFFFB300)),
                center = Offset(x, y),
                radius = radius
            ),
            radius = radius, center = Offset(x, y)
        )
    }

    private fun DrawScope.drawSunRays() {
        val cx = size.width * 0.75f
        val cy = size.height * 0.18f
        val r = size.minDimension * 0.12f
        repeat(12) { i ->
            val angle = Math.toRadians(i * 30.0 + 15.0)
            val startR = r * 1.3f
            val endR = r * 2.0f
            drawLine(
                Color.White.copy(alpha = 0.15f),
                Offset(cx + cos(angle).toFloat() * startR, cy + sin(angle).toFloat() * startR),
                Offset(cx + cos(angle).toFloat() * endR, cy + sin(angle).toFloat() * endR),
                strokeWidth = 3f, cap = StrokeCap.Round
            )
        }
    }

    private fun DrawScope.drawMoon(cx: Float, cy: Float, radius: Float, alpha: Float = 1f) {
        val x = size.width * cx
        val y = size.height * cy
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFE8EAF6).copy(alpha = alpha), Color(0xFFC5CAE9).copy(alpha = alpha)),
                center = Offset(x, y), radius = radius
            ),
            radius = radius, center = Offset(x, y)
        )
        // Crescent cutout
        drawCircle(
            Color(0xFF0A0E27),
            radius = radius * 0.85f,
            center = Offset(x + radius * 0.3f, y - radius * 0.15f)
        )
    }

    private fun DrawScope.drawStars(count: Int) {
        val rng = Random(42)
        repeat(count) {
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height * 0.55f
            val starR = rng.nextFloat() * 2.5f + 1f
            drawCircle(Color.White.copy(alpha = rng.nextFloat() * 0.6f + 0.4f), starR, Offset(x, y))
        }
    }

    private fun DrawScope.drawScatteredCloud(
        cx: Float, cy: Float, scale: Float, alpha: Float = 0.9f
    ) {
        val x = size.width * cx
        val y = size.height * cy
        val baseR = size.minDimension * scale
        drawCircle(Color.White.copy(alpha = alpha), baseR, Offset(x, y))
        drawCircle(Color.White.copy(alpha = alpha * 0.9f), baseR * 0.7f, Offset(x - baseR * 0.6f, y + baseR * 0.2f))
        drawCircle(Color.White.copy(alpha = alpha * 0.9f), baseR * 0.8f, Offset(x + baseR * 0.5f, y + baseR * 0.15f))
        drawCircle(Color.White.copy(alpha = alpha * 0.8f), baseR * 0.5f, Offset(x - baseR * 0.3f, y - baseR * 0.4f))
    }

    private fun DrawScope.drawOvercastCloud(
        cx: Float, cy: Float, width: Float, height: Float, alpha: Float = 0.85f
    ) {
        val x = size.width * cx
        val y = size.height * cy
        val w = size.width * width
        val h = size.height * height
        val cloudColor = Color(0xFF424242).copy(alpha = alpha)
        val cloudColorBright = Color(0xFF616161).copy(alpha = alpha * 0.7f)

        val path = Path().apply {
            moveTo(x - w / 2, y)
            cubicTo(x - w / 2, y - h, x - w / 4, y - h * 1.2f, x, y - h * 0.9f)
            cubicTo(x + w / 6, y - h * 1.1f, x + w / 3, y - h * 0.5f, x + w / 2, y)
            lineTo(x - w / 2, y)
            close()
        }
        drawPath(path, cloudColor)
        // Highlight
        drawPath(path, cloudColorBright)
    }

    private fun DrawScope.drawRain(count: Int) {
        val rng = Random(17)
        repeat(count) {
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            val len = size.height * (rng.nextFloat() * 0.04f + 0.03f)
            drawLine(
                Color.White.copy(alpha = rng.nextFloat() * 0.4f + 0.2f),
                Offset(x, y),
                Offset(x - len * 0.3f, y + len),
                strokeWidth = 1.5f
            )
        }
    }

    private fun DrawScope.drawSnow(count: Int) {
        val rng = Random(23)
        repeat(count) {
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            val r = rng.nextFloat() * 4f + 2f
            drawCircle(Color.White.copy(alpha = rng.nextFloat() * 0.5f + 0.5f), r, Offset(x, y))
        }
    }

    private fun DrawScope.drawLightning(cx: Float, cy: Float) {
        val x = size.width * cx
        val y = size.height * cy
        val h = size.height * 0.25f
        val path = Path().apply {
            moveTo(x, y)
            lineTo(x - h * 0.15f, y + h * 0.4f)
            lineTo(x + h * 0.05f, y + h * 0.4f)
            lineTo(x - h * 0.1f, y + h)
            lineTo(x + h * 0.1f, y + h * 0.35f)
            lineTo(x - h * 0.02f, y + h * 0.35f)
            close()
        }
        drawPath(path, Color(0xFFFFF176).copy(alpha = 0.9f))
        // Glow
        drawPath(
            path,
            brush = Brush.radialGradient(
                listOf(Color(0x80FFF176), Color.Transparent),
                center = Offset(x, y + h * 0.5f),
                radius = h * 0.8f
            )
        )
    }

    private fun DrawScope.drawFogLayer(cy: Float, heightFrac: Float) {
        val y = size.height * cy
        val h = size.height * heightFrac
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.25f), Color.Transparent),
                startY = y - h / 2, endY = y + h / 2
            )
        )
    }

    private fun DrawScope.drawSandParticles(count: Int) {
        val rng = Random(13)
        repeat(count) {
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            drawCircle(
                Color(0xFFD7CCC8).copy(alpha = rng.nextFloat() * 0.3f + 0.1f),
                rng.nextFloat() * 3f + 1f,
                Offset(x, y)
            )
        }
    }

    private fun DrawScope.drawGroundGradient(snowGround: Boolean = false) {
        val bottomColor = if (snowGround) Color(0xFFECEFF1) else Color.Black.copy(alpha = 0.25f)
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, bottomColor),
                startY = size.height * 0.65f,
                endY = size.height
            )
        )
    }
}