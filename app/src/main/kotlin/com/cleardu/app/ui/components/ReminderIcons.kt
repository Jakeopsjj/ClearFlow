package com.cleardu.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Reminder center icon set.
 *
 * Every icon is drawn natively with [Modifier.drawBehind] on a [Box] — no
 * Canvas composable, no SVG asset, no WebView. Coordinates use a 20×20
 * viewBox scaled by `s = size.width / 20f` so an icon renders crisp at any
 * size the caller supplies via [modifier] (default 20.dp).
 *
 * All strokes use [StrokeCap.Round] / [StrokeJoin.Round] to match the
 * liquid-glass design language.
 */

@Composable
fun ReminderBellIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        // Bell body: top center → curve out left → down → bottom → up right → curve back to top
        val bellPath = Path().apply {
            moveTo(10f * s, 3f * s)
            cubicTo(10f * s, 8f * s, 5f * s, 8f * s, 5f * s, 13f * s)
            lineTo(5f * s, 16f * s)
            lineTo(15f * s, 16f * s)
            lineTo(15f * s, 13f * s)
            cubicTo(15f * s, 8f * s, 10f * s, 8f * s, 10f * s, 3f * s)
            close()
        }
        drawPath(
            path = bellPath,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Top stem
        drawLine(tint, Offset(10f * s, 2f * s), Offset(10f * s, 3.5f * s), stroke, StrokeCap.Round)
        // Bottom clapper
        drawCircle(
            color = tint,
            radius = 1f * s,
            center = Offset(10f * s, 17.5f * s),
            style = Stroke(width = stroke)
        )
    })
}

@Composable
fun ReminderPinIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        // Outer teardrop pin: top arc → narrowing down to point
        val pinPath = Path().apply {
            moveTo(10f * s, 3f * s)
            cubicTo(16f * s, 3f * s, 16f * s, 11f * s, 10f * s, 18f * s)
            cubicTo(4f * s, 11f * s, 4f * s, 3f * s, 10f * s, 3f * s)
            close()
        }
        drawPath(
            path = pinPath,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Inner circle at center
        drawCircle(
            color = tint,
            radius = 2.5f * s,
            center = Offset(10f * s, 9f * s),
            style = Stroke(width = stroke)
        )
    })
}

@Composable
fun ReminderPhoneIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        // Classic phone handset path
        val phonePath = Path().apply {
            moveTo(3.5f * s, 5f * s)
            cubicTo(3.5f * s, 4f * s, 4f * s, 3.5f * s, 5f * s, 3.5f * s)
            lineTo(7f * s, 3.5f * s)
            cubicTo(7.5f * s, 3.5f * s, 8f * s, 4f * s, 8f * s, 4.5f * s)
            lineTo(8.5f * s, 7f * s)
            cubicTo(8.5f * s, 7.5f * s, 8f * s, 8f * s, 7.5f * s, 8f * s)
            lineTo(6.5f * s, 8.5f * s)
            cubicTo(7.5f * s, 10.5f * s, 9f * s, 12f * s, 11f * s, 13f * s)
            lineTo(11.5f * s, 12f * s)
            cubicTo(12f * s, 11.5f * s, 12.5f * s, 11f * s, 13f * s, 11f * s)
            lineTo(15.5f * s, 11.5f * s)
            cubicTo(16f * s, 11.5f * s, 16.5f * s, 12f * s, 16.5f * s, 12.5f * s)
            lineTo(16.5f * s, 14.5f * s)
            cubicTo(16.5f * s, 15.5f * s, 16f * s, 16f * s, 15f * s, 16f * s)
            cubicTo(8f * s, 16f * s, 4f * s, 12f * s, 3.5f * s, 5f * s)
            close()
        }
        drawPath(
            path = phonePath,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    })
}

@Composable
fun ReminderStorageIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        // Outer card
        drawRoundRect(
            color = tint,
            topLeft = Offset(4f * s, 3f * s),
            size = Size(12f * s, 14f * s),
            cornerRadius = CornerRadius(2f * s, 2f * s),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Storage stripes
        drawLine(tint, Offset(6.5f * s, 7f * s), Offset(13.5f * s, 7f * s), stroke, StrokeCap.Round)
        drawLine(tint, Offset(6.5f * s, 10f * s), Offset(13.5f * s, 10f * s), stroke, StrokeCap.Round)
        drawLine(tint, Offset(6.5f * s, 13f * s), Offset(13.5f * s, 13f * s), stroke, StrokeCap.Round)
        // Notch dot
        drawCircle(tint, radius = 0.8f * s, center = Offset(13f * s, 5.5f * s))
    })
}

@Composable
fun ReminderAppGridIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        val cellSize = Size(7f * s, 7f * s)
        val corner = CornerRadius(1.5f * s, 1.5f * s)
        val style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // 2×2 grid of rounded squares
        drawRoundRect(tint, Offset(3f * s, 3f * s), cellSize, cornerRadius = corner, style = style)
        drawRoundRect(tint, Offset(10f * s, 3f * s), cellSize, cornerRadius = corner, style = style)
        drawRoundRect(tint, Offset(3f * s, 10f * s), cellSize, cornerRadius = corner, style = style)
        drawRoundRect(tint, Offset(10f * s, 10f * s), cellSize, cornerRadius = corner, style = style)
    })
}

@Composable
fun ReminderStarIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.6f * s
        // Five-pointed star (Heroicons star scaled to 20×20 viewBox)
        val starPath = Path().apply {
            moveTo(10f * s, 1.88f * s)
            lineTo(12.5f * s, 7.24f * s)
            lineTo(18.33f * s, 7.94f * s)
            lineTo(13.75f * s, 11.72f * s)
            lineTo(15.15f * s, 17.5f * s)
            lineTo(10f * s, 14.39f * s)
            lineTo(4.85f * s, 17.5f * s)
            lineTo(6.25f * s, 11.72f * s)
            lineTo(1.67f * s, 7.94f * s)
            lineTo(7.5f * s, 7.24f * s)
            close()
        }
        drawPath(
            path = starPath,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    })
}

@Composable
fun ReminderUsersIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        val headRadius = 2.5f * s
        val bodySize = Size(7f * s, 7f * s)
        val bodyStyle = Stroke(width = stroke, cap = StrokeCap.Round)
        // Left head
        drawCircle(
            color = tint,
            radius = headRadius,
            center = Offset(7f * s, 6f * s),
            style = Stroke(width = stroke)
        )
        // Left body arc (shoulders)
        drawArc(
            color = tint,
            topLeft = Offset(3.5f * s, 10.5f * s),
            size = bodySize,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            style = bodyStyle
        )
        // Right head
        drawCircle(
            color = tint,
            radius = headRadius,
            center = Offset(14f * s, 6f * s),
            style = Stroke(width = stroke)
        )
        // Right body arc (shoulders)
        drawArc(
            color = tint,
            topLeft = Offset(10.5f * s, 10.5f * s),
            size = bodySize,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            style = bodyStyle
        )
    })
}
