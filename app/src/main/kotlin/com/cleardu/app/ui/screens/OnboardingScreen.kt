package com.cleardu.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.PermissionCatalog
import com.cleardu.app.data.PermissionItem
import com.cleardu.app.ui.components.AppLogo
import com.cleardu.app.ui.components.MeshGradientBackground
import com.cleardu.app.ui.components.PermissionCard
import com.cleardu.app.ui.components.StartButton
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Single onboarding / permission screen.
 *
 * Behavior:
 *  - Single page (no HorizontalPager, no page dots, no skip/next buttons)
 *  - Vertical scroll when content overflows
 *  - 6 permission cards; tapping a card / its "允许" button calls [onAllowPermission]
 *  - "开始使用" button calls [onStart] and navigates to the main Activity
 *
 * @param grantedMap current grant state per permission id
 * @param onAllowPermission invoked when the user taps a permission card
 * @param onStart invoked when the user taps the "开始使用" button
 */
@Composable
fun OnboardingScreen(
    grantedMap: Map<String, Boolean>,
    onAllowPermission: (PermissionItem) -> Unit,
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
            Spacer(Modifier.height(ClearDuDimens.PermissionListGap))
            PermissionList(
                items = PermissionCatalog.items,
                grantedMap = grantedMap,
                onAllowPermission = onAllowPermission
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

        Text(
            text = stringResource(com.cleardu.app.R.string.onboarding_title),
            style = ClearDuTypography.WelcomeTitle,
            color = LiquidGlassColors.Foreground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(ClearDuDimens.WelcomeTitleBottomMargin))

        Text(
            text = stringResource(com.cleardu.app.R.string.onboarding_subtitle),
            style = ClearDuTypography.WelcomeSubtitle,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(ClearDuDimens.WelcomeSubtitleBottomMargin))

        Text(
            text = stringResource(com.cleardu.app.R.string.onboarding_desc),
            style = ClearDuTypography.WelcomeDesc,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = ClearDuDimens.WelcomeDescMaxWidth)
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
