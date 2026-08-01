package com.cleardu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.cleardu.app.ui.theme.ClearDuTheme
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Placeholder main Activity reached after onboarding.
 *
 * Real ClearDu modules (dashboard, health-data, reminders, medications, etc.)
 * will hang off this entry point. For the onboarding prototype it shows a
 * confirmation surface so the navigation handoff is verifiable end-to-end.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClearDuTheme {
                MainPlaceholder()
            }
        }
    }
}

@Composable
private fun MainPlaceholder() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.main_placeholder),
                    style = ClearDuTypography.WelcomeTitle,
                    color = LiquidGlassColors.Foreground,
                    textAlign = TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(Modifier.fillMaxSize(0.02f))
                Text(
                    text = stringResource(R.string.main_placeholder_hint),
                    style = ClearDuTypography.WelcomeDesc,
                    color = LiquidGlassColors.Text400,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
