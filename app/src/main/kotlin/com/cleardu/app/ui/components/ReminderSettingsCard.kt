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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

/**
 * 提醒设置卡片 —— 提醒中心页。
 *
 * 复刻参考 HTML 的 `.settings-card`：
 * - 浅色玻璃容器，20dp 圆角，16dp 内边距
 * - 7 条设置项：名称 + 详情 + Toggle
 * - 名称 ReminderSettingName（14sp W500），详情 ReminderSettingDetail（12sp W400）
 * - 详情在名称下方，间距 2dp
 * - 条目之间用 1dp LightDividerSubtle 分隔线（最后一条无分隔线）
 */
@Composable
fun ReminderSettingsCard(
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.ReminderSettingsCardRadius)
    ) {
        Column(
            modifier = Modifier.padding(ClearDuDimens.ReminderSettingsCardPadding)
        ) {
            settings.forEachIndexed { index, item ->
                SettingItemRow(item)
                if (index < settings.lastIndex) {
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
private fun SettingItemRow(item: SettingItem) {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ClearDuDimens.ReminderSettingsItemPaddingV),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = ClearDuDimens.ReminderPermItemGap)
        ) {
            Text(
                text = item.name,
                style = ClearDuTypography.ReminderSettingName,
                color = backgroundAwareColors().foreground
            )
            Spacer(Modifier.height(ClearDuDimens.ReminderSettingsDetailTopGap))
            Text(
                text = item.detail,
                style = ClearDuTypography.ReminderSettingDetail,
                color = backgroundAwareColors().text400
            )
        }
        ReminderToggle(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}

private data class SettingItem(
    val name: String,
    val detail: String
)

private val settings = listOf(
    SettingItem("透析前准备提醒", "提前24小时"),
    SettingItem("透析当日闹钟", "提前2小时"),
    SettingItem("服药提醒", "每次提前15分钟"),
    SettingItem("饮水限制提醒", "每小时"),
    SettingItem("体重监测", "每日晨起"),
    SettingItem("血压监测", "每日2次"),
    SettingItem("紧急联系人一键呼叫", "开启")
)
