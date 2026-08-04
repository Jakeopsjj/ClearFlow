package com.cleardu.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 应用权限卡片 —— 提醒中心页。
 *
 * 复刻参考 HTML 的 `.permission-card`：
 * - 浅色玻璃容器（LightGlass 系列），20dp 圆角，16dp 内边距
 * - 标题“应用权限” + 6 条权限项，每条含图标容器 + 名称/描述 + Toggle
 * - 图标容器 32×32dp，9dp 圆角，白色 0.5 半透明底色（LightGlassBgLight）
 * - 描述文字使用 MedicalGreen（#34C759）
 * - 条目之间用 1dp LightDividerSubtle 分隔线（最后一条无分隔线）
 */
@Composable
fun ReminderPermissionsCard(
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.ReminderPermCardRadius),
        background = LiquidGlassColors.LightGlassBg,
        border = LiquidGlassColors.LightGlassBorder,
        shadowColor = LiquidGlassColors.LightGlassShadow,
        shadowElevation = 4f,
        specularTop = LiquidGlassColors.LightGlassSpecularTop
    ) {
        Column(
            modifier = Modifier.padding(ClearDuDimens.ReminderPermCardPadding)
        ) {
            Text(
                text = "应用权限",
                style = ClearDuTypography.ReminderCardTitle,
                color = LiquidGlassColors.LightForeground
            )
            Spacer(Modifier.height(ClearDuDimens.ReminderPermCardTitleBottomMargin))
            permissions.forEachIndexed { index, item ->
                PermItemRow(item)
                if (index < permissions.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LiquidGlassColors.LightDividerSubtle)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermItemRow(item: PermData) {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ClearDuDimens.ReminderPermItemPaddingV),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.ReminderPermItemGap)
        ) {
            Box(
                modifier = Modifier
                    .size(ClearDuDimens.ReminderPermIconSize)
                    .clip(RoundedCornerShape(ClearDuDimens.ReminderPermIconRadius))
                    .background(LiquidGlassColors.LightGlassBgLight),
                contentAlignment = Alignment.Center
            ) {
                when (item.iconType) {
                    0 -> ReminderBellIcon(
                        item.iconTint,
                        Modifier.size(ClearDuDimens.ReminderPermIconInnerSize)
                    )
                    1 -> ReminderPinIcon(
                        item.iconTint,
                        Modifier.size(ClearDuDimens.ReminderPermIconInnerSize)
                    )
                    2 -> ReminderPhoneIcon(
                        item.iconTint,
                        Modifier.size(ClearDuDimens.ReminderPermIconInnerSize)
                    )
                    3 -> ReminderStorageIcon(
                        item.iconTint,
                        Modifier.size(ClearDuDimens.ReminderPermIconInnerSize)
                    )
                    4 -> ReminderAppGridIcon(
                        item.iconTint,
                        Modifier.size(ClearDuDimens.ReminderPermIconInnerSize)
                    )
                    else -> ReminderStarIcon(
                        item.iconTint,
                        Modifier.size(ClearDuDimens.ReminderPermIconInnerSize)
                    )
                }
            }
            Column {
                Text(
                    text = item.name,
                    style = ClearDuTypography.ReminderPermName,
                    color = LiquidGlassColors.LightForeground
                )
                item.desc?.let {
                    Text(
                        text = it,
                        style = ClearDuTypography.ReminderPermDesc,
                        color = LiquidGlassColors.MedicalGreen
                    )
                }
            }
        }
        ReminderToggle(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}

private data class PermData(
    val name: String,
    val desc: String?,
    val iconTint: Color,
    val iconType: Int
)

private val permissions = listOf(
    PermData("闹钟/提醒", "强提醒模式已激活", LiquidGlassColors.MedicalOrange, 0),
    PermData("定位/导航", null, LiquidGlassColors.MedicalCyan, 1),
    PermData("电话", "一键拨打主治医生", LiquidGlassColors.MedicalGreen, 2),
    PermData("存储", null, LiquidGlassColors.MedicalPurple, 3),
    PermData("应用列表", "用于健康数据导入", LiquidGlassColors.MedicalCyan, 4),
    PermData("后台强提醒", "锁屏全屏提醒", LiquidGlassColors.MedicalRed, 5)
)
