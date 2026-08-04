package com.cleardu.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

/**
 * Medication page icon set.
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
fun MedCapsuleIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.5f * s
        drawRoundRect(
            color = tint,
            topLeft = Offset(3f * s, 6.5f * s),
            size = Size(14f * s, 7f * s),
            cornerRadius = CornerRadius(3.5f * s, 3.5f * s),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Center divider
        drawLine(
            color = tint,
            start = Offset(10f * s, 6.5f * s),
            end = Offset(10f * s, 13.5f * s),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    })
}

@Composable
fun MedTabletIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.5f * s
        withTransform({
            rotate(degrees = -30f, pivot = Offset(10f * s, 10f * s))
        }) {
            drawOval(
                color = tint,
                topLeft = Offset(4f * s, 6f * s),
                size = Size(12f * s, 8f * s),
                style = Stroke(width = stroke, join = StrokeJoin.Round)
            )
            // Score line across the long axis (becomes diagonal after rotation)
            drawLine(
                color = tint,
                start = Offset(4f * s, 10f * s),
                end = Offset(16f * s, 10f * s),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    })
}

@Composable
fun MedBoxIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.5f * s
        val thin = 1.2f * s
        // Box body
        drawRoundRect(
            color = tint,
            topLeft = Offset(4f * s, 8f * s),
            size = Size(12f * s, 9f * s),
            cornerRadius = CornerRadius(2f * s, 2f * s),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // V / peaked lid opening on top
        drawLine(tint, Offset(4f * s, 8f * s), Offset(10f * s, 4f * s), stroke, StrokeCap.Round)
        drawLine(tint, Offset(10f * s, 4f * s), Offset(16f * s, 8f * s), stroke, StrokeCap.Round)
        // Center blister divider
        drawLine(tint, Offset(10f * s, 8f * s), Offset(10f * s, 17f * s), thin, StrokeCap.Round)
    })
}

@Composable
fun MedInjectionIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.5f * s
        val thin = 1.1f * s
        // Plunger handle
        drawLine(tint, Offset(10f * s, 2f * s), Offset(10f * s, 4.5f * s), stroke, StrokeCap.Round)
        // Top flange bar
        drawLine(tint, Offset(6.5f * s, 4.5f * s), Offset(13.5f * s, 4.5f * s), stroke, StrokeCap.Round)
        // Tube barrel
        drawRoundRect(
            color = tint,
            topLeft = Offset(8f * s, 4.5f * s),
            size = Size(4f * s, 9f * s),
            cornerRadius = CornerRadius(1f * s, 1f * s),
            style = Stroke(width = stroke, join = StrokeJoin.Round)
        )
        // Needle
        drawLine(tint, Offset(10f * s, 13.5f * s), Offset(10f * s, 18f * s), thin, StrokeCap.Round)
    })
}

@Composable
fun MedMoonIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.5f * s
        val zStroke = 1.2f * s

        // Crescent = outer circle minus an offset inner circle (opens to the right)
        val outer = Path().apply {
            addOval(Rect(2f * s, 3f * s, 14f * s, 15f * s)) // center (8,9) r=6
        }
        val inner = Path().apply {
            addOval(Rect(6f * s, 4f * s, 16f * s, 14f * s)) // center (11,9) r=5
        }
        val moon = Path().apply {
            op(outer, inner, PathOperation.Difference)
        }
        drawPath(
            path = moon,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Z mark at upper-right
        drawLine(tint, Offset(14f * s, 3f * s), Offset(16.5f * s, 3f * s), zStroke, StrokeCap.Round)
        drawLine(tint, Offset(16.5f * s, 3f * s), Offset(14f * s, 5.5f * s), zStroke, StrokeCap.Round)
        drawLine(tint, Offset(14f * s, 5.5f * s), Offset(16.5f * s, 5.5f * s), zStroke, StrokeCap.Round)
    })
}

@Composable
fun MedWarningTriangleIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.5f * s
        val triPath = Path().apply {
            moveTo(10f * s, 3f * s)
            lineTo(3f * s, 16f * s)
            lineTo(17f * s, 16f * s)
            close()
        }
        drawPath(
            path = triPath,
            color = tint,
            style = Stroke(width = stroke, join = StrokeJoin.Round)
        )
        // Exclamation bar
        drawLine(tint, Offset(10f * s, 8f * s), Offset(10f * s, 11.5f * s), stroke, StrokeCap.Round)
        // Exclamation dot
        drawCircle(tint, radius = 0.9f * s, center = Offset(10f * s, 13.8f * s))
    })
}

@Composable
fun MedClockIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.5f * s
        drawCircle(
            color = tint,
            radius = 7f * s,
            center = Offset(10f * s, 10f * s),
            style = Stroke(width = stroke)
        )
        // Hour hand (up)
        drawLine(tint, Offset(10f * s, 10f * s), Offset(10f * s, 6f * s), stroke, StrokeCap.Round)
        // Minute hand (right)
        drawLine(tint, Offset(10f * s, 10f * s), Offset(13f * s, 10f * s), stroke, StrokeCap.Round)
    })
}

@Composable
fun MedGearIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.5f * s
        val cx = 10f * s
        val cy = 10f * s
        val r1 = 4.5f * s
        val r2 = 7.5f * s
        // Eight radiating teeth
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45).toDouble()).toFloat()
            val cos = kotlin.math.cos(angle.toDouble()).toFloat()
            val sin = kotlin.math.sin(angle.toDouble()).toFloat()
            drawLine(
                color = tint,
                start = Offset(cx + r1 * cos, cy + r1 * sin),
                end = Offset(cx + r2 * cos, cy + r2 * sin),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
        // Center hub
        drawCircle(
            color = tint,
            radius = 3f * s,
            center = Offset(cx, cy),
            style = Stroke(width = stroke)
        )
    })
}

@Composable
fun MedCheckIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        drawLine(tint, Offset(4f * s, 10.5f * s), Offset(8.5f * s, 15f * s), stroke, StrokeCap.Round)
        drawLine(tint, Offset(8.5f * s, 15f * s), Offset(16f * s, 6.5f * s), stroke, StrokeCap.Round)
    })
}

@Composable
fun MedChevronRightIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        drawLine(tint, Offset(8f * s, 5f * s), Offset(13f * s, 10f * s), stroke, StrokeCap.Round)
        drawLine(tint, Offset(13f * s, 10f * s), Offset(8f * s, 15f * s), stroke, StrokeCap.Round)
    })
}

@Composable
fun MedPlusIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Box(modifier = modifier.drawBehind {
        val s = size.width / 20f
        val stroke = 1.8f * s
        drawLine(tint, Offset(4f * s, 10f * s), Offset(16f * s, 10f * s), stroke, StrokeCap.Round)
        drawLine(tint, Offset(10f * s, 4f * s), Offset(10f * s, 16f * s), stroke, StrokeCap.Round)
    })
}

/**
 * Picks a medication icon matching the drug name.
 *
 * Used by [MedicationTimeline] so the [MedicationDose] data class can stay
 * free of an icon-type field while still rendering the right glyph.
 */
@Composable
internal fun MedicationDoseIcon(
    name: String,
    tint: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    when {
        name.contains("注射") -> MedInjectionIcon(tint, modifier)
        name.contains("安眠") -> MedMoonIcon(tint, modifier)
        name.contains("磷") -> MedTabletIcon(tint, modifier)
        name.contains("铁") -> MedTabletIcon(tint, modifier)
        else -> MedCapsuleIcon(tint, modifier)
    }
}
