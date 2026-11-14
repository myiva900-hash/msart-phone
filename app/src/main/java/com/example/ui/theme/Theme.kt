package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun YvanAndroidTheme(
    darkTheme: Boolean = true,
    accentPreset: YvanAccentPreset = YvanAccentPreset.EMERALD,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accentPreset.primaryDark,
            onPrimary = Color(0xFF04120B),
            primaryContainer = accentPreset.primaryDark.copy(alpha = 0.18f),
            onPrimaryContainer = accentPreset.primaryDark,
            secondary = accentPreset.secondaryDark,
            onSecondary = Color(0xFF04101A),
            secondaryContainer = accentPreset.secondaryDark.copy(alpha = 0.16f),
            onSecondaryContainer = accentPreset.secondaryDark,
            tertiary = accentPreset.tertiaryDark,
            background = CyberObsidian,
            onBackground = Color(0xFFF0F4FA),
            surface = CyberSurface,
            onSurface = Color(0xFFF0F4FA),
            surfaceVariant = CyberSurfaceVariant,
            onSurfaceVariant = Color(0xFFA7B5CE),
            outline = CyberOutline,
            error = RootWarningRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = accentPreset.primaryLight,
            onPrimary = Color.White,
            primaryContainer = accentPreset.primaryLight.copy(alpha = 0.14f),
            onPrimaryContainer = accentPreset.primaryLight,
            secondary = accentPreset.secondaryLight,
            onSecondary = Color.White,
            secondaryContainer = accentPreset.secondaryLight.copy(alpha = 0.14f),
            onSecondaryContainer = accentPreset.secondaryLight,
            tertiary = accentPreset.tertiaryLight,
            background = LightBackground,
            onBackground = Color(0xFF0F172A),
            surface = LightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = Color(0xFF475569),
            outline = LightOutline,
            error = Color(0xFFD32F2F),
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YvanTypography,
        content = content
    )
}
