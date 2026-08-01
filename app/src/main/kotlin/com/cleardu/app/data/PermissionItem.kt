package com.cleardu.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.ui.graphics.vector.ImageVector
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Color tone associated with a permission card. Each tone resolves to a
 * background tint + border + icon foreground color, mirroring the reference
 * `.perm-icon-{tone}` classes.
 */
enum class PermissionTone(
    val tintBackground: androidx.compose.ui.graphics.Color,
    val tintBorder: androidx.compose.ui.graphics.Color,
    val iconColor: androidx.compose.ui.graphics.Color
) {
    Cyan(
        tintBackground = LiquidGlassColors.TintCyanBg,
        tintBorder = LiquidGlassColors.TintCyanBorder,
        iconColor = LiquidGlassColors.MedicalCyan
    ),
    Orange(
        tintBackground = LiquidGlassColors.TintOrangeBg,
        tintBorder = LiquidGlassColors.TintOrangeBorder,
        iconColor = LiquidGlassColors.MedicalOrange
    ),
    Purple(
        tintBackground = LiquidGlassColors.TintPurpleBg,
        tintBorder = LiquidGlassColors.TintPurpleBorder,
        iconColor = LiquidGlassColors.MedicalPurple
    ),
    Green(
        tintBackground = LiquidGlassColors.TintGreenBg,
        tintBorder = LiquidGlassColors.TintGreenBorder,
        iconColor = LiquidGlassColors.MedicalGreen
    ),
    Indigo(
        tintBackground = LiquidGlassColors.TintIndigoBg,
        tintBorder = LiquidGlassColors.TintIndigoBorder,
        iconColor = LiquidGlassColors.MedicalIndigo
    ),
    Red(
        tintBackground = LiquidGlassColors.TintRedBg,
        tintBorder = LiquidGlassColors.TintRedBorder,
        iconColor = LiquidGlassColors.MedicalRed
    );
}

/**
 * Immutable descriptor for a single onboarding permission card.
 *
 * @param id stable identifier (used as state key + for analytics)
 * @param nameRes display name resource id (e.g. R.string.perm_location_name)
 * @param descRes description resource id
 * @param icon Material icon shown in the tinted circle
 * @param tone color tone of the icon circle
 * @param critical when true the card is rendered with the red critical badge
 *                 and the red/orange background tint
 */
data class PermissionItem(
    val id: String,
    val nameRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val tone: PermissionTone,
    val critical: Boolean = false
)

/**
 * Canonical onboarding permission set, in display order.
 * Order matches the reference HTML permission list.
 */
object PermissionCatalog {

    val items: List<PermissionItem> = listOf(
        PermissionItem(
            id = "location",
            nameRes = com.cleardu.app.R.string.perm_location_name,
            descRes = com.cleardu.app.R.string.perm_location_desc,
            icon = Icons.Outlined.LocationOn,
            tone = PermissionTone.Cyan
        ),
        PermissionItem(
            id = "alarm",
            nameRes = com.cleardu.app.R.string.perm_alarm_name,
            descRes = com.cleardu.app.R.string.perm_alarm_desc,
            icon = Icons.Outlined.Alarm,
            tone = PermissionTone.Orange
        ),
        PermissionItem(
            id = "storage",
            nameRes = com.cleardu.app.R.string.perm_storage_name,
            descRes = com.cleardu.app.R.string.perm_storage_desc,
            icon = Icons.Outlined.Storage,
            tone = PermissionTone.Purple
        ),
        PermissionItem(
            id = "phone",
            nameRes = com.cleardu.app.R.string.perm_phone_name,
            descRes = com.cleardu.app.R.string.perm_phone_desc,
            icon = Icons.Outlined.PhoneInTalk,
            tone = PermissionTone.Green
        ),
        PermissionItem(
            id = "apps",
            nameRes = com.cleardu.app.R.string.perm_apps_name,
            descRes = com.cleardu.app.R.string.perm_apps_desc,
            icon = Icons.Outlined.Apps,
            tone = PermissionTone.Indigo
        ),
        PermissionItem(
            id = "alert",
            nameRes = com.cleardu.app.R.string.perm_alert_name,
            descRes = com.cleardu.app.R.string.perm_alert_desc,
            icon = Icons.Outlined.NotificationsActive,
            tone = PermissionTone.Red,
            critical = true
        )
    )
}
