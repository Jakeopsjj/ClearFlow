package com.cleardu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.LiquidGlassColors

// ===== Vital Card Icons (16x16 viewBox) =====

@Composable
fun BloodPressureIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val s = w / 16f
        val stroke1 = 1.5f * s
        val stroke2 = 1.2f * s
        val c = LiquidGlassColors.MedicalRed

        drawLine(c, Offset(8f * s, 2f * s), Offset(8f * s, 4f * s), stroke1, StrokeCap.Round)
        drawLine(c, Offset(8f * s, 12f * s), Offset(8f * s, 14f * s), stroke1, StrokeCap.Round)
        drawLine(c, Offset(2f * s, 8f * s), Offset(4f * s, 8f * s), stroke1, StrokeCap.Round)
        drawLine(c, Offset(12f * s, 8f * s), Offset(14f * s, 8f * s), stroke1, StrokeCap.Round)
        drawCircle(c, radius = 3f * s, center = Offset(8f * s, 8f * s), style = Stroke(stroke1))
        drawLine(c, Offset(8f * s, 6f * s), Offset(8f * s, 8f * s), stroke2, StrokeCap.Round)
        drawLine(c, Offset(8f * s, 8f * s), Offset(9.5f * s, 9.5f * s), stroke2, StrokeCap.Round)
    }
}

@Composable
fun HeartRateIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val s = w / 16f
        val stroke = 1f * s

        val heartPath = Path().apply {
            moveTo(8f * s, 14f * s)
            cubicTo(8f * s, 14f * s, 3f * s, 10.5f * s, 3f * s, 6.5f * s)
            cubicTo(3f * s, 4.8f * s, 4.3f * s, 3.5f * s, 5.5f * s, 3.5f * s)
            cubicTo(6.4f * s, 3.5f * s, 7.2f * s, 3.9f * s, 8f * s, 4.8f * s)
            cubicTo(8.8f * s, 3.9f * s, 9.6f * s, 3.5f * s, 10.5f * s, 3.5f * s)
            cubicTo(11.7f * s, 3.5f * s, 13f * s, 4.8f * s, 13f * s, 6.5f * s)
            cubicTo(13f * s, 10.5f * s, 8f * s, 14f * s, 8f * s, 14f * s)
            close()
        }
        drawPath(heartPath, LiquidGlassColors.MedicalRed.copy(alpha = 0.9f))

        val ecgPath = Path().apply {
            moveTo(5.5f * s, 8f * s)
            lineTo(7.5f * s, 8f * s)
            lineTo(8.5f * s, 6f * s)
            lineTo(9.5f * s, 10f * s)
            lineTo(10.5f * s, 8f * s)
            lineTo(12.5f * s, 8f * s)
        }
        drawPath(ecgPath, Color.White, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun WeightIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val s = w / 16f
        val stroke = 1.3f * s
        val c = LiquidGlassColors.MedicalCyan

        drawRoundRect(c, Offset(2f * s, 5f * s), Size(12f * s, 8f * s),
            CornerRadius(2f * s), style = Stroke(stroke))
        drawLine(c, Offset(5f * s, 5f * s), Offset(5f * s, 4f * s), stroke, StrokeCap.Round)
        drawLine(c, Offset(11f * s, 5f * s), Offset(11f * s, 4f * s), stroke, StrokeCap.Round)
        val arcPath = Path().apply {
            moveTo(5f * s, 4f * s)
            cubicTo(5f * s, 2.34f * s, 6.34f * s, 1f * s, 8f * s, 1f * s)
            cubicTo(9.66f * s, 1f * s, 11f * s, 2.34f * s, 11f * s, 4f * s)
        }
        drawPath(arcPath, c, style = Stroke(stroke, cap = StrokeCap.Round))
        drawCircle(c, radius = 1.2f * s, center = Offset(8f * s, 9f * s))
        drawLine(c, Offset(7.2f * s, 8.8f * s), Offset(6.4f * s, 8f * s), 1f * s, StrokeCap.Round)
        drawLine(c, Offset(8.8f * s, 9.2f * s), Offset(9.6f * s, 10f * s), 1f * s, StrokeCap.Round)
    }
}

@Composable
fun TemperatureIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val s = w / 16f
        val stroke = 1.3f * s
        val c = LiquidGlassColors.MedicalOrange

        val bodyPath = Path().apply {
            moveTo(6f * s, 2f * s)
            cubicTo(6f * s, 0.9f * s, 6.9f * s, 0f * s, 8f * s, 0f * s)
            cubicTo(9.1f * s, 0f * s, 10f * s, 0.9f * s, 10f * s, 2f * s)
            lineTo(10f * s, 7.5f * s)
            cubicTo(10f * s, 9.16f * s, 8.66f * s, 10.5f * s, 7f * s, 10.5f * s)
            cubicTo(5.34f * s, 10.5f * s, 4f * s, 9.16f * s, 4f * s, 7.5f * s)
            lineTo(4f * s, 4f * s)
            cubicTo(4f * s, 2.9f * s, 4.9f * s, 2f * s, 6f * s, 2f * s)
            close()
        }
        drawPath(bodyPath, c, style = Stroke(stroke))
        drawCircle(c, radius = 1.2f * s, center = Offset(8f * s, 11f * s))
        drawLine(c, Offset(8f * s, 5f * s), Offset(8f * s, 9f * s), stroke, StrokeCap.Round)
    }
}

// ===== Medication Reminder Icon (20x20 viewBox) =====

@Composable
fun MedicationPillIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val s = w / 20f
        val stroke = 1.5f * s
        val thin = 1.2f * s
        val c = LiquidGlassColors.MedicalOrange

        drawRoundRect(LiquidGlassColors.TintOrangeBg, Offset(3f * s, 7f * s),
            Size(14f * s, 7f * s), CornerRadius(2f * s))
        drawRoundRect(c, Offset(3f * s, 7f * s), Size(14f * s, 7f * s),
            CornerRadius(2f * s), style = Stroke(stroke))
        drawLine(c, Offset(10f * s, 7f * s), Offset(10f * s, 14f * s), thin)
        drawLine(c, Offset(3f * s, 10.5f * s), Offset(17f * s, 10.5f * s), thin)
        drawLine(c, Offset(7f * s, 3.5f * s), Offset(10f * s, 6.5f * s), thin, StrokeCap.Round)
        drawLine(c, Offset(10f * s, 3.5f * s), Offset(13f * s, 6.5f * s), thin, StrokeCap.Round)
    }
}

// ===== Quick Action Icons (22x22 viewBox) =====

@Composable
fun RecordUltrafiltrationIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val s = w / 22f
        val stroke = 2f * s
        val c = LiquidGlassColors.MedicalCyan
        drawLine(c, Offset(11f * s, 4f * s), Offset(11f * s, 18f * s), stroke, StrokeCap.Round)
        drawLine(c, Offset(4f * s, 11f * s), Offset(18f * s, 11f * s), stroke, StrokeCap.Round)
    }
}

@Composable
fun MeasureBpIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val s = w / 22f
        val stroke = 1.8f * s
        val thin = 1.3f * s

        val heartPath = Path().apply {
            moveTo(11f * s, 18f * s)
            cubicTo(11f * s, 18f * s, 5f * s, 13.8f * s, 5f * s, 9f * s)
            cubicTo(5f * s, 6.79f * s, 6.79f * s, 5f * s, 9f * s, 5f * s)
            cubicTo(10.2f * s, 5f * s, 11.3f * s, 5.5f * s, 12f * s, 6.2f * s)
            cubicTo(12.7f * s, 5.5f * s, 13.8f * s, 5f * s, 15f * s, 5f * s)
            cubicTo(17.21f * s, 5f * s, 19f * s, 6.79f * s, 19f * s, 9f * s)
            cubicTo(19f * s, 13.8f * s, 11f * s, 18f * s, 11f * s, 18f * s)
            close()
        }
        drawPath(heartPath, LiquidGlassColors.TintRedBg)
        drawPath(heartPath, LiquidGlassColors.MedicalRed, style = Stroke(stroke))

        val ecgPath = Path().apply {
            moveTo(8.5f * s, 10f * s)
            lineTo(10.5f * s, 10f * s)
            lineTo(11.5f * s, 7.5f * s)
            lineTo(12.5f * s, 12.5f * s)
            lineTo(13.5f * s, 10f * s)
            lineTo(15.5f * s, 10f * s)
        }
        drawPath(ecgPath, LiquidGlassColors.MedicalRed,
            style = Stroke(thin, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun RecordMedicationIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val s = w / 22f
        val stroke = 1.8f * s
        val thin = 1.3f * s
        val c = LiquidGlassColors.MedicalPurple

        drawRoundRect(LiquidGlassColors.TintPurpleBg, Offset(4f * s, 7f * s),
            Size(14f * s, 9f * s), CornerRadius(2.5f * s))
        drawRoundRect(c, Offset(4f * s, 7f * s), Size(14f * s, 9f * s),
            CornerRadius(2.5f * s), style = Stroke(stroke))
        drawLine(c, Offset(11f * s, 7f * s), Offset(11f * s, 16f * s), thin)
        drawLine(c, Offset(7.5f * s, 10f * s), Offset(9.5f * s, 12f * s), thin, StrokeCap.Round)
        drawLine(c, Offset(14.5f * s, 10f * s), Offset(12.5f * s, 12f * s), thin, StrokeCap.Round)
    }
}

@Composable
fun DrinkWaterIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val s = w / 22f
        val stroke = 1.8f * s
        val thin = 1.5f * s
        val c = LiquidGlassColors.MedicalCyan

        val bodyPath = Path().apply {
            moveTo(11f * s, 5f * s)
            cubicTo(8f * s, 5f * s, 5.5f * s, 7f * s, 5.5f * s, 10f * s)
            cubicTo(5.5f * s, 12f * s, 6.7f * s, 13.5f * s, 8f * s, 14.5f * s)
            lineTo(8f * s, 16f * s)
            cubicTo(8f * s, 16.55f * s, 8.45f * s, 17f * s, 9f * s, 17f * s)
            lineTo(13f * s, 17f * s)
            cubicTo(13.55f * s, 17f * s, 14f * s, 16.55f * s, 14f * s, 16f * s)
            lineTo(14f * s, 14.5f * s)
            cubicTo(15.3f * s, 13.5f * s, 16.5f * s, 12f * s, 16.5f * s, 10f * s)
            cubicTo(16.5f * s, 7f * s, 14f * s, 5f * s, 11f * s, 5f * s)
            close()
        }
        drawPath(bodyPath, LiquidGlassColors.TintCyanBg)
        drawPath(bodyPath, c, style = Stroke(stroke))
        drawLine(c, Offset(9.5f * s, 18.5f * s), Offset(14.5f * s, 18.5f * s), thin, StrokeCap.Round)
    }
}

// ===== Navigation Bar Icons (24x24 viewBox) =====

@Composable
fun NavHomeIcon(modifier: Modifier = Modifier, tint: Color = LiquidGlassColors.Text400) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val s = w / 24f
        val stroke = 1.8f * s

        val path = Path().apply {
            moveTo(3f * s, 11f * s)
            lineTo(12f * s, 3f * s)
            lineTo(21f * s, 11f * s)
            lineTo(21f * s, 21f * s)
            cubicTo(21f * s, 21.55f * s, 20.55f * s, 22f * s, 20f * s, 22f * s)
            lineTo(15f * s, 22f * s)
            lineTo(15f * s, 15f * s)
            lineTo(9f * s, 15f * s)
            lineTo(9f * s, 22f * s)
            lineTo(4f * s, 22f * s)
            cubicTo(3.45f * s, 22f * s, 3f * s, 21.55f * s, 3f * s, 21f * s)
            lineTo(3f * s, 11f * s)
            close()
        }
        drawPath(path, tint, style = Stroke(stroke, join = StrokeJoin.Round))
    }
}

@Composable
fun NavRecordIcon(modifier: Modifier = Modifier, tint: Color = LiquidGlassColors.Text400) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val s = w / 24f
        val stroke = 1.8f * s
        drawCircle(tint, radius = 9f * s, center = Offset(12f * s, 12f * s), style = Stroke(stroke))
        drawLine(tint, Offset(12f * s, 8f * s), Offset(12f * s, 16f * s), stroke, StrokeCap.Round)
        drawLine(tint, Offset(8f * s, 12f * s), Offset(16f * s, 12f * s), stroke, StrokeCap.Round)
    }
}

@Composable
fun NavDataIcon(modifier: Modifier = Modifier, tint: Color = LiquidGlassColors.Text400) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val s = w / 24f
        val stroke = 1.8f * s

        val path = Path().apply {
            moveTo(4f * s, 20f * s)
            lineTo(4f * s, 10f * s)
            moveTo(10f * s, 20f * s)
            lineTo(10f * s, 4f * s)
            moveTo(16f * s, 20f * s)
            lineTo(16f * s, 13f * s)
            moveTo(22f * s, 20f * s)
            lineTo(2f * s, 20f * s)
        }
        drawPath(path, tint, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun NavMedicationIcon(modifier: Modifier = Modifier, tint: Color = LiquidGlassColors.Text400) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val s = w / 24f
        val stroke = 1.8f * s
        val cx = 12f * s
        val cy = 12f * s
        val angle = Math.toRadians(-45.0).toFloat()
        val cos = kotlin.math.cos(angle.toDouble()).toFloat()
        val sin = kotlin.math.sin(angle.toDouble()).toFloat()
        val halfW = 9f * s
        val halfH = 4f * s

        fun rot(x: Float, y: Float): Offset = Offset(
            cx + (x - cx) * cos - (y - cy) * sin,
            cy + (x - cx) * sin + (y - cy) * cos
        )

        val path = Path().apply {
            val p0 = rot(3f * s, 8f * s)
            val p1 = rot(21f * s, 8f * s)
            val p2 = rot(21f * s, 16f * s)
            val p3 = rot(3f * s, 16f * s)
            moveTo(p0.x, p0.y)
            lineTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            close()
        }
        drawPath(path, tint, style = Stroke(stroke))

        val ls = rot(8.5f * s, 7.5f * s)
        val le = rot(15.5f * s, 14.5f * s)
        drawLine(tint, ls, le, stroke, StrokeCap.Round)
    }
}

@Composable
fun NavRemindersIcon(modifier: Modifier = Modifier, tint: Color = LiquidGlassColors.Text400) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val s = w / 24f
        val stroke = 1.8f * s

        val path = Path().apply {
            moveTo(6f * s, 16f * s)
            lineTo(6f * s, 11f * s)
            cubicTo(6f * s, 7.69f * s, 8.69f * s, 5f * s, 12f * s, 5f * s)
            cubicTo(15.31f * s, 5f * s, 18f * s, 7.69f * s, 18f * s, 11f * s)
            lineTo(18f * s, 16f * s)
            lineTo(19.5f * s, 18f * s)
            lineTo(4.5f * s, 18f * s)
            lineTo(6f * s, 16f * s)
            close()
        }
        drawPath(path, tint, style = Stroke(stroke, join = StrokeJoin.Round))

        drawArc(tint, 0f, 180f, false,
            Offset(10f * s, 19f * s), Size(4f * s, 2f * s),
            style = Stroke(stroke, cap = StrokeCap.Round))
    }
}