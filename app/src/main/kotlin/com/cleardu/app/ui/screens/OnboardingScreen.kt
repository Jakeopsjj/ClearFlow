package com.cleardu.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleardu.app.R
import com.cleardu.app.data.PermissionCatalog
import com.cleardu.app.data.PermissionItem
import com.cleardu.app.ui.components.AppLogo
import com.cleardu.app.ui.components.MeshGradientBackground
import com.cleardu.app.ui.components.PermissionCard
import com.cleardu.app.ui.components.StartButton
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuMotion
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Single onboarding / permission screen.
 *
 * Features:
 *  - Warm welcome message with app logo
 *  - Permission section title with description
 *  - 6 permission cards; tapping a card calls [onAllowPermission]
 *  - "一键授予全部权限" button for batch granting
 *  - "开始使用" button calls [onStart] and navigates to the main Activity
 *
 * @param grantedMap current grant state per permission id
 * @param isGrantingAll true when batch granting is in progress
 * @param onAllowPermission invoked when the user taps a permission card
 * @param onGrantAll invoked when the user taps the one-click grant button
 * @param onStart invoked when the user taps the "开始使用" button
 */
@Composable
fun OnboardingScreen(
    grantedMap: Map<String, Boolean>,
    isGrantingAll: Boolean = false,
    onAllowPermission: (PermissionItem) -> Unit,
    onGrantAll: () -> Unit = {},
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allGranted = PermissionCatalog.items.all { grantedMap[it.id] == true }

    MeshGradientBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = ClearDuDimens.ScreenHorizontalPadding,
                    end = ClearDuDimens.ScreenHorizontalPadding,
                    top = ClearDuDimens.ScreenTopPadding,
                    bottom = ClearDuDimens.ScreenBottomScrollPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            WelcomeSection()
            Spacer(Modifier.height(20.dp))
            PermissionSectionHeader()
            Spacer(Modifier.height(ClearDuDimens.PermissionListGap))
            PermissionList(
                items = PermissionCatalog.items,
                grantedMap = grantedMap,
                onAllowPermission = onAllowPermission
            )
            Spacer(Modifier.height(16.dp))
            GrantAllButton(
                isGranting = isGrantingAll,
                allGranted = allGranted,
                onClick = onGrantAll
            )
            Spacer(Modifier.height(ClearDuDimens.PermissionListBottomMargin))
            BottomSection(
                allGranted = allGranted,
                onStart = onStart
            )
        }
    }
}

@Composable
private fun WelcomeSection() {
    Column(
        modifier = Modifier
            .padding(top = ClearDuDimens.WelcomeTopPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()
        Spacer(Modifier.height(ClearDuDimens.WelcomeLogoBottomMargin))

        // Warm greeting
        Text(
            text = stringResource(R.string.onboarding_welcome_line1),
            style = ClearDuTypography.WelcomeSubtitle,
            color = LiquidGlassColors.Foreground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_line2),
            style = ClearDuTypography.WelcomeDesc,
            color = LiquidGlassColors.Text300,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_title),
            style = ClearDuTypography.WelcomeTitle,
            color = LiquidGlassColors.Foreground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(ClearDuDimens.WelcomeTitleBottomMargin))

        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = ClearDuTypography.WelcomeSubtitle,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(ClearDuDimens.WelcomeSubtitleBottomMargin))

        Text(
            text = stringResource(R.string.onboarding_desc),
            style = ClearDuTypography.WelcomeDesc,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = ClearDuDimens.WelcomeDescMaxWidth)
        )
    }
}

@Composable
private fun PermissionSectionHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_perm_title),
            style = ClearDuTypography.WelcomeSubtitle,
            color = LiquidGlassColors.Foreground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.onboarding_perm_subtitle),
            style = ClearDuTypography.WelcomeDesc,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionList(
    items: List<PermissionItem>,
    grantedMap: Map<String, Boolean>,
    onAllowPermission: (PermissionItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(ClearDuDimens.PermissionListGap)) {
        items.forEach { item ->
            PermissionCard(
                item = item,
                granted = grantedMap[item.id] == true,
                onAllow = { onAllowPermission(item) }
            )
        }
    }
}

/**
 * "一键授予全部权限" button — prominent gradient button styled like the start button
 * but with a different color scheme (softer). Shows a loading spinner when granting.
 */
@Composable
private fun GrantAllButton(
    isGranting: Boolean,
    allGranted: Boolean,
    onClick: () -> Unit
) {
    if (allGranted) return // Hide when all permissions are already granted

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (pressed || isGranting) 0.96f else 1f,
        animationSpec = spring(),
        label = "grantAllScale"
    )

    val transition = rememberInfiniteTransition(label = "grantAllGlow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "grantAllGlowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 280.dp)
            .height(48.dp)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true, color = Color.White),
                enabled = !isGranting,
                onClick = onClick
            )
            .drawWithContent {
                // Glow halo
                val glowBrush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlassColors.MedicalCyan.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                    radius = maxOf(size.width, size.height) * 0.75f
                )
                drawRect(brush = glowBrush)

                // Background gradient
                val bgBrush = Brush.linearGradient(
                    colors = listOf(
                        LiquidGlassColors.MedicalCyan.copy(alpha = 0.25f),
                        LiquidGlassColors.MedicalBlue.copy(alpha = 0.20f),
                        LiquidGlassColors.MedicalPurple.copy(alpha = 0.18f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                )
                drawRect(brush = bgBrush)

                drawContent()

                // Specular top highlight
                val specBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.5f
                )
                drawRect(brush = specBrush)
            }
            .border(
                BorderStroke(1.dp, LiquidGlassColors.MedicalCyan.copy(alpha = 0.35f)),
                RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isGranting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LiquidGlassColors.MedicalCyan
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_granting),
                    style = ClearDuTypography.PermissionButton.copy(fontSize = 14.sp),
                    color = LiquidGlassColors.Foreground
                )
            } else {
                Text(
                    text = stringResource(R.string.action_grant_all),
                    style = ClearDuTypography.PermissionButton.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W700
                    ),
                    color = LiquidGlassColors.Foreground
                )
            }
        }
    }
}

@Composable
private fun BottomSection(allGranted: Boolean, onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ClearDuDimens.BottomSectionGap)
    ) {
        AgreementText()
        StartButton(
            allGranted = allGranted,
            onClick = onStart,
            modifier = Modifier.padding(bottom = ClearDuDimens.BottomSectionBottomPadding)
        )
    }
}

@Composable
private fun AgreementText() {
    val annotated = buildAnnotatedString {
        append("点击即表示您同意")
        withStyle(SpanStyle(color = LiquidGlassColors.MedicalCyan)) {
            append("《用户协议》")
        }
        append("和")
        withStyle(SpanStyle(color = LiquidGlassColors.MedicalCyan)) {
            append("《隐私政策》")
        }
    }
    Text(
        text = annotated,
        style = ClearDuTypography.Agreement,
        color = LiquidGlassColors.Text400,
        textAlign = TextAlign.Center
    )
}
