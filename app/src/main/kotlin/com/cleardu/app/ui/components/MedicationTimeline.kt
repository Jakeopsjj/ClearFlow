package com.cleardu.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

/**
 * 一次服药记录的状态。
 */
enum class MedDoseStatus { TAKEN, UPCOMING, NEXT_DOSE, OPTIONAL }

/**
 * 时间线上的单次服药数据。
 */
data class MedicationDose(
    val time: String,
    val name: String,
    val dose: String,
    val instruction: String?,
    val status: MedDoseStatus,
    val iconTint: Color,
    val iconBg: Color
)

/**
 * 今日用药时间线。
 *
 * 左侧时间列 + 时间线/状态点 + 右侧玻璃药品卡片。
 * 点击"服了"按钮可将 [MedDoseStatus.NEXT_DOSE] 项切换为 [MedDoseStatus.TAKEN]。
 * 列表为空时显示空状态提示，引导用户通过 FAB 添加用药。
 *
 * @param doses 当日用药列表，为空时展示空状态
 * @param onDoseTaken 服药确认回调
 * @param modifier 外部 modifier
 */
@Composable
fun MedicationTimeline(
    doses: List<MedicationDose> = emptyList(),
    onDoseTaken: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 追踪刚点击"服了"的索引，保持卡片不透明
    val justTaken = remember { mutableStateListOf<Int>() }

    val timeColWidth = ClearDuDimens.MedTimelineTimeColWidth
    val gap = ClearDuDimens.MedCardGap
    val dotSize = ClearDuDimens.MedTimelineDotSize
    val lineWidth = ClearDuDimens.MedTimelineLineWidth
    // 时间线贴近时间列，距卡片更远，视觉更舒适
    val lineCenterX = timeColWidth + ClearDuDimens.MedTimelineLineOffsetFromTime
    val dotLeft = lineCenterX - dotSize / 2

    if (doses.isEmpty()) {
        // === 空状态 ===
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MedClockIcon(
                    tint = LiquidGlassColors.Text400.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "暂无用药记录",
                    style = ClearDuTypography.MedSectionLabel,
                    color = backgroundAwareColors().text400
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "点击右下角 + 添加用药时间",
                    style = ClearDuTypography.MedCardMeta,
                    color = backgroundAwareColors().text400.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val x = lineCenterX.toPx()
                val topPad = 18.dp.toPx()
                drawLine(
                    color = LiquidGlassColors.LightGlassBg,
                    start = Offset(x, topPad),
                    end = Offset(x, size.height - topPad),
                    strokeWidth = lineWidth.toPx(),
                    cap = StrokeCap.Round
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ClearDuDimens.MedTimelineItemGap)
        ) {
            doses.forEachIndexed { index, dose ->
                MedicationTimelineItem(
                    dose = dose,
                    justTaken = index in justTaken,
                    lineDotLeft = dotLeft,
                    onTake = {
                        if (dose.status == MedDoseStatus.NEXT_DOSE ||
                            dose.status == MedDoseStatus.UPCOMING
                        ) {
                            if (index !in justTaken) justTaken.add(index)
                            onDoseTaken(index)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MedicationTimelineItem(
    dose: MedicationDose,
    justTaken: Boolean,
    lineDotLeft: Dp,
    onTake: () -> Unit
) {
    val isPast = dose.status == MedDoseStatus.TAKEN
    // 历史 TAKEN 卡片 0.6 透明；刚点击"服了"的卡片保持不透明
    val cardAlpha = if (isPast && !justTaken) 0.6f else 1f

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedCardGap)
        ) {
            // Time column
            Box(
                modifier = Modifier
                    .width(ClearDuDimens.MedTimelineTimeColWidth)
                    .padding(top = 14.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = dose.time,
                    style = ClearDuTypography.MedTimelineTime,
                    color = if (isPast) backgroundAwareColors().text400 else backgroundAwareColors().foreground,
                    textAlign = TextAlign.End
                )
            }

            // Medication card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .alpha(cardAlpha),
                shape = RoundedCornerShape(ClearDuDimens.MedCardRadius)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = ClearDuDimens.MedCardPaddingH,
                            vertical = ClearDuDimens.MedCardPaddingV
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedCardGap)
                ) {
                    // Drug icon
                    Box(
                        modifier = Modifier
                            .size(ClearDuDimens.MedCardIconSize)
                            .clip(RoundedCornerShape(ClearDuDimens.MedCardIconRadius))
                            .drawBehind { drawRect(dose.iconBg) },
                        contentAlignment = Alignment.Center
                    ) {
                        MedicationDoseIcon(
                            name = dose.name,
                            tint = dose.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dose.name,
                            style = ClearDuTypography.MedCardName,
                            color = backgroundAwareColors().foreground
                        )
                        Spacer(Modifier.height(ClearDuDimens.MedCardNameBottomGap))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedCardMetaGap)
                        ) {
                            Text(
                                text = dose.dose,
                                style = ClearDuTypography.MedCardDose,
                                color = backgroundAwareColors().text300
                            )
                            if (!dose.instruction.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(ClearDuDimens.MedCardDividerSize)
                                        .drawBehind {
                                            drawCircle(LiquidGlassColors.LightDividerDot)
                                        }
                                )
                                Text(
                                    text = dose.instruction,
                                    style = ClearDuTypography.MedCardMeta,
                                    color = backgroundAwareColors().text400
                                )
                            }
                        }
                    }

                    // Status indicator
                    StatusIndicator(dose = dose, onTake = onTake)
                }
            }
        }

        // Timeline dot overlay (sits on the vertical line)
        TimelineDot(
            status = dose.status,
            modifier = Modifier
                .offset(x = lineDotLeft, y = 18.dp)
                .size(ClearDuDimens.MedTimelineDotSize)
        )
    }
}

@Composable
private fun StatusIndicator(dose: MedicationDose, onTake: () -> Unit) {
    when (dose.status) {
        MedDoseStatus.TAKEN -> {
            Box(
                modifier = Modifier
                    .size(ClearDuDimens.MedStatusCheckSize)
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawRect(LiquidGlassColors.LightTintGreenStrong) }
                    .border(
                        BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.MedicalGreen),
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                MedCheckIcon(
                    tint = LiquidGlassColors.MedicalGreen,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        MedDoseStatus.NEXT_DOSE -> TakeButton(onClick = onTake)

        MedDoseStatus.UPCOMING -> {
            Box(
                modifier = Modifier
                    .size(ClearDuDimens.MedStatusCheckSize)
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawRect(LiquidGlassColors.LightCircleEmptyBg) }
                    .border(
                        BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.LightCircleEmptyBorder),
                        RoundedCornerShape(50)
                    )
            )
        }

        MedDoseStatus.OPTIONAL -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawRect(LiquidGlassColors.LightMuted) }
                    .border(
                        BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.LightBorderMedium),
                        RoundedCornerShape(50)
                    )
                    .padding(
                        horizontal = ClearDuDimens.MedOptionalLabelPaddingH,
                        vertical = ClearDuDimens.MedOptionalLabelPaddingV
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "可选",
                    style = ClearDuTypography.MedOptionalLabel,
                    color = backgroundAwareColors().text400
                )
            }
        }
    }
}

@Composable
private fun TakeButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        label = "takeBtnScale"
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(50))
            .drawBehind { drawRect(LiquidGlassColors.LightTintCyanMd) }
            .border(
                BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.LightTintCyanActive),
                RoundedCornerShape(50)
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = ClearDuDimens.MedTakeBtnPaddingH,
                vertical = ClearDuDimens.MedTakeBtnPaddingV
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "服了",
            style = ClearDuTypography.MedTakeBtn,
            color = LiquidGlassColors.MedicalCyan
        )
    }
}

@Composable
private fun TimelineDot(status: MedDoseStatus, modifier: Modifier = Modifier) {
    when (status) {
        MedDoseStatus.TAKEN -> {
            Box(modifier = modifier.drawBehind {
                val r = size.minDimension / 2f
                // Glow
                drawCircle(
                    LiquidGlassColors.MedicalGreen.copy(alpha = 0.35f),
                    radius = r * 1.7f
                )
                drawCircle(LiquidGlassColors.MedicalGreen, radius = r)
            })
        }

        MedDoseStatus.UPCOMING -> {
            Box(
                modifier = modifier
                    .drawBehind {
                        val r = size.minDimension / 2f
                        drawCircle(Color(0x26000000), radius = r)
                    }
                    .border(
                        BorderStroke(ClearDuDimens.GlassBorderWidth, Color(0x40000000)),
                        RoundedCornerShape(50)
                    )
            )
        }

        MedDoseStatus.NEXT_DOSE -> {
            // Pulsing glow (alpha 0.25 -> 0.5 over 2s)
            val transition = rememberInfiniteTransition(label = "nextDosePulse")
            val pulse by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "nextDosePulseAlpha"
            )
            Box(modifier = modifier.drawBehind {
                val r = size.minDimension / 2f
                drawCircle(
                    LiquidGlassColors.MedicalCyan.copy(alpha = pulse),
                    radius = r * 1.8f
                )
                drawCircle(LiquidGlassColors.MedicalCyan, radius = r)
            })
        }

        MedDoseStatus.OPTIONAL -> {
            val dash = PathEffect.dashPathEffect(
                floatArrayOf(3f, 3f)
            )
            Box(
                modifier = modifier
                    .drawBehind {
                        val r = size.minDimension / 2f
                        drawCircle(Color(0x14000000), radius = r)
                        drawCircle(
                            color = LiquidGlassColors.LightBorderMedium,
                            radius = r,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = dash)
                        )
                    }
            )
        }
    }
}
