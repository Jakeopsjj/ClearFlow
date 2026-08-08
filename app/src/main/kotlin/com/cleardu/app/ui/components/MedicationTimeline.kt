package com.cleardu.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors
import com.cleardu.app.ui.theme.glassParams

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
    val glass = glassParams()
    val colors = backgroundAwareColors()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.MedCardRadius),
        background = glass.background,
        border = glass.border,
        shadowColor = glass.shadowColor,
        shadowElevation = glass.shadowElevation,
        specularTop = glass.specularTop
    ) {
        if (doses.isEmpty()) {
            // === 空状态（与 ReminderTodayList 一致） ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MedClockIcon(
                    tint = colors.text400.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "暂无用药记录",
                    style = ClearDuTypography.MedSectionLabel,
                    color = colors.text400
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "点击右下角 + 添加用药时间",
                    style = ClearDuTypography.MedCardMeta,
                    color = colors.text400.copy(alpha = 0.6f)
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(
                    horizontal = ClearDuDimens.MedCardPaddingH,
                    vertical = ClearDuDimens.MedCardPaddingV
                )
            ) {
                doses.forEachIndexed { index, dose ->
                    // 时间线行（类似 ReminderTodayList 的 TodayReminderRow）
                    MedicationTimelineRow(
                        dose = dose,
                        justTaken = index in justTaken,
                        onTake = {
                            if (dose.status == MedDoseStatus.NEXT_DOSE ||
                                dose.status == MedDoseStatus.UPCOMING
                            ) {
                                if (index !in justTaken) justTaken.add(index)
                                onDoseTaken(index)
                            }
                        }
                    )
                    // 分隔线（最后一条不显示）
                    if (index < doses.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(LiquidGlassColors.DividerSubtle)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationTimelineRow(
    dose: MedicationDose,
    justTaken: Boolean,
    onTake: () -> Unit
) {
    val isPast = dose.status == MedDoseStatus.TAKEN
    val cardAlpha = if (isPast && !justTaken) 0.6f else 1f
    val colors = backgroundAwareColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .padding(vertical = ClearDuDimens.MedTimelineItemGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedCardGap)
    ) {
        // 时间线圆点（类似 ReminderTodayList 的脉冲圆点）
        TimelineDotNew(
            status = dose.status,
            modifier = Modifier.size(ClearDuDimens.MedTimelineDotSize)
        )

        // 时间
        Text(
            text = dose.time,
            style = ClearDuTypography.MedTimelineTime,
            color = if (isPast) colors.text400 else colors.foreground,
            textAlign = TextAlign.End,
            modifier = Modifier.width(ClearDuDimens.MedTimelineTimeColWidth)
        )

        // 药品图标 + 名称 + 剂量
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

        // 药品详情
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dose.name,
                style = ClearDuTypography.MedCardName,
                color = colors.foreground
            )
            Spacer(Modifier.height(ClearDuDimens.MedCardNameBottomGap))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedCardMetaGap)
            ) {
                Text(
                    text = dose.dose,
                    style = ClearDuTypography.MedCardDose,
                    color = colors.text300
                )
                if (!dose.instruction.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(ClearDuDimens.MedCardDividerSize)
                            .drawBehind {
                                drawCircle(LiquidGlassColors.DividerSubtle)
                            }
                    )
                    Text(
                        text = dose.instruction,
                        style = ClearDuTypography.MedCardMeta,
                        color = colors.text400
                    )
                }
            }
        }

        // 状态指示器
        MedStatusBadge(dose = dose, onTake = onTake)
    }
}

@Composable
private fun TimelineDotNew(status: MedDoseStatus, modifier: Modifier = Modifier) {
    when (status) {
        MedDoseStatus.TAKEN -> {
            Box(
                modifier = modifier
                    .drawBehind {
                        val r = size.minDimension / 2f
                        drawCircle(
                            LiquidGlassColors.MedicalGreen.copy(alpha = 0.35f),
                            radius = r * 1.5f
                        )
                        drawCircle(LiquidGlassColors.MedicalGreen, radius = r)
                    }
            )
        }
        MedDoseStatus.NEXT_DOSE -> {
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
                    radius = r * 1.5f
                )
                drawCircle(LiquidGlassColors.MedicalCyan, radius = r)
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
                        CircleShape
                    )
            )
        }
        MedDoseStatus.OPTIONAL -> {
            val dash = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))
            Box(
                modifier = modifier
                    .drawBehind {
                        val r = size.minDimension / 2f
                        drawCircle(Color(0x14000000), radius = r)
                        drawCircle(
                            color = LiquidGlassColors.DividerMedium,
                            radius = r,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = dash)
                        )
                    }
            )
        }
    }
}

@Composable
private fun MedStatusBadge(dose: MedicationDose, onTake: () -> Unit) {
    val colors = backgroundAwareColors()
    when (dose.status) {
        MedDoseStatus.TAKEN -> {
            Box(
                modifier = Modifier
                    .size(ClearDuDimens.MedStatusCheckSize)
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawRect(LiquidGlassColors.TintGreenStrong) }
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
        MedDoseStatus.NEXT_DOSE -> MedTakeButton(onClick = onTake)
        MedDoseStatus.UPCOMING -> {
            Box(
                modifier = Modifier
                    .size(ClearDuDimens.MedStatusCheckSize)
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawRect(LiquidGlassColors.GlassBgLight) }
                    .border(
                        BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.GlassBorderSubtle),
                        RoundedCornerShape(50)
                    )
            )
        }
        MedDoseStatus.OPTIONAL -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawRect(LiquidGlassColors.DividerLight) }
                    .border(
                        BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.DividerMedium),
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
                    color = colors.text400
                )
            }
        }
    }
}

@Composable
private fun MedTakeButton(onClick: () -> Unit) {
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
            .drawBehind { drawRect(LiquidGlassColors.TintCyanMd) }
            .border(
                BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.TintCyanActive),
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
