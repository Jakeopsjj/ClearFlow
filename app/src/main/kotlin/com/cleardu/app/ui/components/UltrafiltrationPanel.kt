package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors
import java.util.Locale

/**
 * "超滤量" tab content for the data-record page.
 *
 * Layout (top-to-bottom, all inside a vertically scrollable parent):
 * 1. Input Ring — circular progress ring with value display ("___" placeholder)
 * 2. Quick Adjust — four glass pills (-100/-50/+50/+100 ml)
 * 3. Number Keypad — 3×4 grid (1-9, ".", 0, delete)
 * 4. Goal Progress — daily goal bar ("今日已记录 1,200 / 目标 2,500ml")
 *
 * @param inputValue current ultrafiltration value in ml
 * @param goalTarget daily goal target in ml
 * @param todayRecorded today's already recorded amount in ml
 * @param onValueChange callback when the input value changes
 * @param modifier outer modifier
 */
@Composable
fun UltrafiltrationPanel(
    inputValue: Int = 0,
    goalTarget: Int = 2500,
    todayRecorded: Int = 1200,
    onValueChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var rawInput by remember { mutableStateOf(if (inputValue > 0) inputValue.toString() else "") }

    // Sync rawInput when inputValue changes from parent (e.g. data loaded from DataStore)
    // Only sync when the numeric values differ, so we don't overwrite user's typing
    LaunchedEffect(inputValue) {
        val currentNumeric = rawInput.toIntOrNull() ?: 0
        if (currentNumeric != inputValue) {
            rawInput = if (inputValue > 0) inputValue.toString() else ""
        }
    }

    val displayValue = if (rawInput.isEmpty()) "___" else rawInput
    val numericValue = rawInput.toDoubleOrNull() ?: 0.0
    val goalTargetD = goalTarget.toDouble()
    val goalCurrentD = todayRecorded.toDouble()
    val progressFraction = (goalCurrentD / goalTargetD).toFloat().coerceIn(0f, 1f)
    val ringFraction = (numericValue / goalTargetD).toFloat().coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        // === Input Ring ===
        InputRing(
            displayValue = displayValue,
            isPlaceholder = rawInput.isEmpty(),
            progressFraction = ringFraction
        )
        Spacer(Modifier.height(ClearDuDimens.InputRingBottomMargin))

        // === Quick Adjust Buttons ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.QuickAdjustGap)
        ) {
            listOf("-100ml", "-50ml", "+50ml", "+100ml").forEach { label ->
                val delta = label.removeSuffix("ml").toInt()
                QuickAdjustButton(
                    label = label,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val current = rawInput.toIntOrNull() ?: 0
                        val result = (current + delta).coerceAtLeast(0)
                        rawInput = result.toString()
                        onValueChange(result)
                    }
                )
            }
        }
        Spacer(Modifier.height(ClearDuDimens.QuickAdjustBottomMargin))

        // === Number Keypad ===
        Keypad(
            onKey = { key ->
                when (key) {
                    "del" -> {
                        if (rawInput.isNotEmpty()) {
                            rawInput = rawInput.dropLast(1)
                            onValueChange(rawInput.toIntOrNull() ?: 0)
                        }
                    }
                    "." -> {
                        if (!rawInput.contains(".")) {
                            rawInput += "."
                        }
                    }
                    else -> {
                        if (rawInput.length < 6) {
                            rawInput += key
                            onValueChange(rawInput.toIntOrNull() ?: 0)
                        }
                    }
                }
            }
        )
        Spacer(Modifier.height(ClearDuDimens.KeypadBottomMargin))

        // === Goal Progress ===
        GoalProgressBar(
            current = todayRecorded,
            target = goalTarget,
            fraction = progressFraction
        )
    }
}

// ===== Input Ring =====

@Composable
private fun InputRing(
    displayValue: String,
    isPlaceholder: Boolean,
    progressFraction: Float
) {
    val colors = backgroundAwareColors()
    val inputUnitColor = if (colors.isBright) colors.unitGreen else colors.text400
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(ClearDuDimens.InputRingSize),
            contentAlignment = Alignment.Center
        ) {
            // SVG-like ring drawn on Canvas
            Canvas(modifier = Modifier.size(ClearDuDimens.InputRingSize)) {
                val strokeW = ClearDuDimens.InputRingStrokeWidth.toPx()
                val radius = ClearDuDimens.InputRingRadius.dp.toPx()
                val center = Offset(this.size.width / 2f, this.size.height / 2f)

                // Track circle
                drawCircle(
                    color = if (colors.isBright) LiquidGlassColors.BrightGlassBg else LiquidGlassColors.GlassBgLight,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeW)
                )

                // Progress arc (270° sweep from -225° to 45°)
                if (progressFraction > 0f) {
                    val sweepAngle = 360f * progressFraction
                    drawArc(
                        color = LiquidGlassColors.MedicalCyan,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        ),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
            }

            // Center text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayValue,
                    style = ClearDuTypography.InputDisplay,
                    color = if (isPlaceholder) LiquidGlassColors.PlaceholderText else colors.foreground
                )
                Text(
                    text = "ml",
                    style = ClearDuTypography.InputUnit,
                    color = inputUnitColor
                )
            }
        }
        Spacer(Modifier.height(ClearDuDimens.InputRingLabelTopMargin))
        Text(
            text = "点击数字键盘输入超滤量",
            style = ClearDuTypography.InputLabel,
            color = inputUnitColor
        )
    }
}

// ===== Quick Adjust Button =====

@Composable
private fun QuickAdjustButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        label = "quickAdjustScale"
    )

    GlassCard(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Text(
            text = label,
            style = ClearDuTypography.QuickAdjustText,
            color = LiquidGlassColors.MedicalCyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                horizontal = ClearDuDimens.QuickAdjustBtnPaddingH,
                vertical = ClearDuDimens.QuickAdjustBtnPaddingV
            )
        )
    }
}

// ===== Keypad =====

@Composable
private fun Keypad(onKey: (String) -> Unit) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "del")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ClearDuDimens.KeypadGap)
    ) {
        keys.chunked(3).forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.KeypadGap)
            ) {
                rowKeys.forEach { key ->
                    KeypadKey(
                        key = key,
                        onClick = { onKey(key) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        label = "keypadKeyScale"
    )
    val colors = backgroundAwareColors()
    val keyBg = if (colors.isBright) colors.glassBg else LiquidGlassColors.KeyBg
    val keyActiveBg = if (colors.isBright) colors.glassBg.copy(alpha = 0.5f) else LiquidGlassColors.KeyActiveBg

    GlassCard(
        modifier = modifier
            .height(ClearDuDimens.KeypadKeyHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(ClearDuDimens.KeypadKeyRadius))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(ClearDuDimens.KeypadKeyRadius),
        background = if (pressed) keyActiveBg else keyBg,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (key == "del") {
                DeleteIcon()
            } else {
                Text(
                    text = key,
                    style = ClearDuTypography.KeyText,
                    color = if (key == ".") LiquidGlassColors.KeyActionColor else LiquidGlassColors.KeyText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DeleteIcon() {
    Canvas(modifier = Modifier.size(22.dp)) {
        val s = size.width / 24f
        val stroke = 1.8f * s

        // Delete key outline (trapezoid with rounded corner)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(21f * s, 5f * s)
            lineTo(9f * s, 5f * s)
            lineTo(2f * s, 12f * s)
            lineTo(9f * s, 19f * s)
            lineTo(21f * s, 19f * s)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = 19f * s,
                    top = 5f * s,
                    right = 23f * s,
                    bottom = 19f * s
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(path, LiquidGlassColors.Text400, style = Stroke(stroke))

        // X lines
        drawLine(
            LiquidGlassColors.Text400,
            Offset(12f * s, 9f * s),
            Offset(18f * s, 15f * s),
            stroke,
            StrokeCap.Round
        )
        drawLine(
            LiquidGlassColors.Text400,
            Offset(18f * s, 9f * s),
            Offset(12f * s, 15f * s),
            stroke,
            StrokeCap.Round
        )
    }
}

// ===== Goal Progress =====

@Composable
private fun GoalProgressBar(
    current: Int,
    target: Int,
    fraction: Float
) {
    val colors = backgroundAwareColors()
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600),
        label = "goalProgress"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.GoalProgressRadius),
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = ClearDuDimens.GoalProgressPaddingH,
                vertical = ClearDuDimens.GoalProgressPaddingV
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "今日已记录",
                    style = ClearDuTypography.GoalLabel,
                    color = colors.text400
                )
                Text(
                    text = String.format(Locale.US, "%,d / 目标 %,dml", current, target),
                    style = ClearDuTypography.GoalValues,
                    color = colors.foreground
                )
            }
            Spacer(Modifier.height(8.dp))
            // Progress bar track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ClearDuDimens.GoalBarTrackHeight)
                    .clip(RoundedCornerShape(ClearDuDimens.GoalBarRadius))
                    .drawBehind {
                        drawRect(LiquidGlassColors.GlassBgLight)
                    }
            ) {
                // Progress fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .height(ClearDuDimens.GoalBarTrackHeight)
                        .clip(RoundedCornerShape(ClearDuDimens.GoalBarRadius))
                        .drawBehind {
                            drawRect(
                                Brush.linearGradient(
                                    colors = listOf(
                                        LiquidGlassColors.MedicalCyan,
                                        LiquidGlassColors.MedicalBlue
                                    )
                                )
                            )
                        }
                )
            }
        }
    }
}
