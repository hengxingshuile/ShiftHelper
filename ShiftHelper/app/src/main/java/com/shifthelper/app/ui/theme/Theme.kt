package com.shifthelper.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SafariBlue,
    onPrimary = Color.White,
    primaryContainer = SafariBlue.copy(alpha = 0.12f),
    onPrimaryContainer = SafariBlue,
    secondary = SafariGreen,
    onSecondary = Color.White,
    secondaryContainer = SafariGreen.copy(alpha = 0.12f),
    onSecondaryContainer = SafariGreen,
    tertiary = SafariOrange,
    onTertiary = Color.White,
    tertiaryContainer = SafariOrange.copy(alpha = 0.12f),
    onTertiaryContainer = SafariOrange,
    background = SafariBackground,
    onBackground = SafariLabel,
    surface = SafariSurface,
    onSurface = SafariLabel,
    surfaceVariant = SafariSecondarySurface,
    onSurfaceVariant = SafariSecondaryLabel,
    error = SafariRed,
    onError = Color.White,
    outline = SafariTertiaryLabel
)

private val DarkColorScheme = darkColorScheme(
    primary = SafariBlueLight,
    onPrimary = Color.White,
    primaryContainer = SafariBlueLight.copy(alpha = 0.15f),
    onPrimaryContainer = SafariBlueLight,
    secondary = SafariGreen,
    onSecondary = Color.White,
    secondaryContainer = SafariGreen.copy(alpha = 0.15f),
    onSecondaryContainer = SafariGreen,
    tertiary = SafariOrange,
    onTertiary = Color.White,
    tertiaryContainer = SafariOrange.copy(alpha = 0.15f),
    onTertiaryContainer = SafariOrange,
    background = SafariBackgroundDark,
    onBackground = SafariLabelDark,
    surface = SafariSurfaceDark,
    onSurface = SafariLabelDark,
    surfaceVariant = SafariSecondarySurfaceDark,
    onSurfaceVariant = SafariSecondaryLabel,
    error = SafariRed,
    onError = Color.White,
    outline = SafariSecondaryLabel
)

@Composable
fun ShiftHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SafariTypography,
        content = content
    )
}
