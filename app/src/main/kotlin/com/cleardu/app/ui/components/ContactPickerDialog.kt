package com.cleardu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cleardu.app.data.AppSettings
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Contact picker dialog for emergency contact selection.
 *
 * Shows a list of mock contacts (in production, would use ContactsContract API)
 * plus a custom input option for manual entry.
 *
 * @param currentSettings current app settings
 * @param onSave callback with updated settings
 * @param onDismiss dismiss callback
 */
@Composable
fun ContactPickerDialog(
    currentSettings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=通讯录, 1=自定义
    var customName by remember { mutableStateOf("") }
    var customPhone by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Mock contacts (in production, read from ContactsContract)
    val mockContacts = remember {
        listOf(
            ContactInfo("李医生", "138-0000-1001", "主治医师"),
            ContactInfo("王护士长", "139-0000-2002", "血液净化中心"),
            ContactInfo("张主任", "136-0000-3003", "肾内科主任"),
            ContactInfo("急诊科", "120", "医院急诊"),
            ContactInfo("家人（配偶）", "137-0000-4004", "紧急联系人"),
            ContactInfo("家人（子女）", "135-0000-5005", "紧急联系人")
        )
    }

    val filteredContacts = remember(searchQuery) {
        if (searchQuery.isBlank()) mockContacts
        else mockContacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(24.dp))
                .background(LiquidGlassColors.LightBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择紧急联系人",
                        style = ClearDuTypography.MedPageTitle,
                        color = LiquidGlassColors.LightForeground
                    )
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = LiquidGlassColors.Text400)
                    }
                }

                // Tab row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabChip("通讯录", selected = selectedTab == 0) { selectedTab = 0 }
                    TabChip("自定义", selected = selectedTab == 1) { selectedTab = 1 }
                }

                Spacer(Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // Search bar
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        background = LiquidGlassColors.GlassBg,
                        border = LiquidGlassColors.GlassBorder,
                        specularTop = LiquidGlassColors.GlassSpecularTop
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            textStyle = ClearDuTypography.MedSearchPlaceholder.copy(
                                color = LiquidGlassColors.LightForeground
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                            decorationBox = { inner ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "搜索联系人...",
                                            style = ClearDuTypography.MedSearchPlaceholder,
                                            color = LiquidGlassColors.PlaceholderInput
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Contact list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredContacts.forEach { contact ->
                            ContactItem(
                                contact = contact,
                                isSelected = currentSettings.emergencyContactPhone == contact.phone,
                                onClick = {
                                    onSave(
                                        currentSettings.copy(
                                            emergencyContactName = contact.name,
                                            emergencyContactPhone = contact.phone,
                                            emergencyContactIsCustom = false
                                        )
                                    )
                                    onDismiss()
                                }
                            )
                        }
                    }
                } else {
                    // Custom input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            background = LiquidGlassColors.GlassBg,
                            border = LiquidGlassColors.GlassBorder,
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("联系人姓名", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = ClearDuTypography.MedSearchPlaceholder.copy(
                                        color = LiquidGlassColors.LightForeground
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                    decorationBox = { inner ->
                                        if (customName.isEmpty()) {
                                            Text("输入联系人姓名", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            background = LiquidGlassColors.GlassBg,
                            border = LiquidGlassColors.GlassBorder,
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("电话号码", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = customPhone,
                                    onValueChange = { customPhone = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = ClearDuTypography.MedSearchPlaceholder.copy(
                                        color = LiquidGlassColors.LightForeground
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                    decorationBox = { inner ->
                                        if (customPhone.isEmpty()) {
                                            Text("输入电话号码", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (customName.isNotBlank() && customPhone.isNotBlank()) LiquidGlassColors.MedicalCyan
                                    else LiquidGlassColors.Text400.copy(alpha = 0.3f)
                                )
                                .clickable(enabled = customName.isNotBlank() && customPhone.isNotBlank()) {
                                    onSave(
                                        currentSettings.copy(
                                            emergencyContactName = customName.trim(),
                                            emergencyContactPhone = customPhone.trim(),
                                            emergencyContactIsCustom = true
                                        )
                                    )
                                    onDismiss()
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "保存自定义联系人",
                                style = ClearDuTypography.MedTakeBtn,
                                color = LiquidGlassColors.White
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ContactInfo(
    val name: String,
    val phone: String,
    val role: String = ""
)

@Composable
private fun ContactItem(
    contact: ContactInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        background = if (isSelected) LiquidGlassColors.TintCyanMd else LiquidGlassColors.GlassBg,
        border = if (isSelected) LiquidGlassColors.MedicalCyan.copy(alpha = 0.3f) else LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        style = ClearDuTypography.MedItemName,
                        color = if (isSelected) LiquidGlassColors.MedicalCyan else LiquidGlassColors.LightForeground
                    )
                    if (contact.role.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = contact.role,
                            style = ClearDuTypography.MedCardMeta,
                            color = LiquidGlassColors.Text400
                        )
                    }
                }
                Text(
                    text = contact.phone,
                    style = ClearDuTypography.MedDetail,
                    color = LiquidGlassColors.Text400
                )
            }
        }
    }
}