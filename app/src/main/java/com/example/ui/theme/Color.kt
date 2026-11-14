package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Core Cyber Obsidian & Light Surface Neutrals
val CyberObsidian = Color(0xFF070B14)
val CyberSurface = Color(0xFF0E1525)
val CyberSurfaceVariant = Color(0xFF162038)
val CyberOutline = Color(0xFF283552)

val LightBackground = Color(0xFFF3F6FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE5ECF6)
val LightOutline = Color(0xFFC2CEE0)

// Accent Theme Presets
enum class YvanAccentPreset(
    val key: String,
    val labelEn: String,
    val labelRw: String,
    val primaryDark: Color,
    val secondaryDark: Color,
    val tertiaryDark: Color,
    val primaryLight: Color,
    val secondaryLight: Color,
    val tertiaryLight: Color
) {
    EMERALD(
        key = "EMERALD",
        labelEn = "Emerald Cyber",
        labelRw = "Icyatsi Cyber",
        primaryDark = Color(0xFF00E676),
        secondaryDark = Color(0xFF00E5FF),
        tertiaryDark = Color(0xFF69F0AE),
        primaryLight = Color(0xFF008744),
        secondaryLight = Color(0xFF007799),
        tertiaryLight = Color(0xFF00A859)
    ),
    COBALT(
        key = "COBALT",
        labelEn = "Cobalt Neon",
        labelRw = "Ubururu Neon",
        primaryDark = Color(0xFF40C4FF),
        secondaryDark = Color(0xFF7C4DFF),
        tertiaryDark = Color(0xFF18FFFF),
        primaryLight = Color(0xFF0066CC),
        secondaryLight = Color(0xFF5333ED),
        tertiaryLight = Color(0xFF0088AA)
    ),
    CRIMSON(
        key = "CRIMSON",
        labelEn = "Crimson Root",
        labelRw = "Umutuku Root",
        primaryDark = Color(0xFFFF4081),
        secondaryDark = Color(0xFFFFAB40),
        tertiaryDark = Color(0xFFFF80AB),
        primaryLight = Color(0xFFC2185B),
        secondaryLight = Color(0xFFD84315),
        tertiaryLight = Color(0xFFE91E63)
    ),
    AMBER(
        key = "AMBER",
        labelEn = "Amber Gold",
        labelRw = "Zahabu Amber",
        primaryDark = Color(0xFFFFCA28),
        secondaryDark = Color(0xFF00E676),
        tertiaryDark = Color(0xFFFFE082),
        primaryLight = Color(0xFFB27B00),
        secondaryLight = Color(0xFF007E33),
        tertiaryLight = Color(0xFFD99300)
    );

    companion object {
        fun fromKey(key: String): YvanAccentPreset =
            entries.firstOrNull { it.key == key } ?: EMERALD
    }
}

val RootWarningRed = Color(0xFFFF5252)
val RootSuccessGreen = Color(0xFF00E676)
