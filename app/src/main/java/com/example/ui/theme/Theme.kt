package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisDarkColorScheme = darkColorScheme(
    primary = ArcCyanPrimary,
    onPrimary = HudBackground,
    primaryContainer = HudSurfaceVariant,
    onPrimaryContainer = ArcCyanGlow,
    secondary = IronGold,
    onSecondary = HudBackground,
    secondaryContainer = HudSurfaceElevated,
    onSecondaryContainer = IronGoldGlow,
    tertiary = ReactorBlue,
    onTertiary = TextPrimary,
    background = HudBackground,
    onBackground = TextPrimary,
    surface = HudSurface,
    onSurface = TextPrimary,
    surfaceVariant = HudSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = HudBorder,
    outlineVariant = HudBorderBright,
    error = IronRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = HudBackground.toArgb()
                window.navigationBarColor = HudBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = JarvisDarkColorScheme,
        typography = Typography,
        content = content
    )
}
