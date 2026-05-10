package com.locationjoystick.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Primary action color — joystick, buttons, FABs */
val LjBlue = Color(0xFF3B82F6)
val LjBlueDark = Color(0xFF1D4ED8)
val LjBlueLight = Color(0xFF93C5FD)
val LjBlueContainer = Color(0xFF1E3A5F)
val LjOnBlueContainer = Color(0xFFBFDBFE)

/** Secondary color — active GPS indicator, success state */
val LjGreen = Color(0xFF22C55E)
val LjGreenDark = Color(0xFF15803D)
val LjGreenLight = Color(0xFF86EFAC)
val LjGreenContainer = Color(0xFF14532D)
val LjOnGreenContainer = Color(0xFFBBF7D0)

/** Error/stopped state */
val LjRed = Color(0xFFEF4444)
val LjRedDark = Color(0xFFB91C1C)
val LjRedLight = Color(0xFFFCA5A5)
val LjRedContainer = Color(0xFF450A0A)
val LjOnRedContainer = Color(0xFFFECACA)

/** Neutral surfaces */
val LjSurfaceDark = Color(0xFF0F172A)
val LjSurfaceDarkVariant = Color(0xFF1E293B)
val LjSurfaceDarkElevated = Color(0xFF334155)

val LjSurfaceLight = Color(0xFFF8FAFC)
val LjSurfaceLightVariant = Color(0xFFE2E8F0)
val LjSurfaceLightElevated = Color(0xFFFFFFFF)

/** Text on dark surfaces */
val LjOnSurfaceDark = Color(0xFFF1F5F9)
val LjOnSurfaceDarkVariant = Color(0xFF94A3B8)

/** Text on light surfaces */
val LjOnSurfaceLight = Color(0xFF0F172A)
val LjOnSurfaceLightVariant = Color(0xFF64748B)

/** Outline */
val LjOutlineDark = Color(0xFF475569)
val LjOutlineLight = Color(0xFFCBD5E1)

val LjDarkColorScheme = darkColorScheme(
    primary = LjBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LjBlueContainer,
    onPrimaryContainer = LjOnBlueContainer,

    secondary = LjGreen,
    onSecondary = Color(0xFF052E16),
    secondaryContainer = LjGreenContainer,
    onSecondaryContainer = LjOnGreenContainer,

    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF1E003F),
    tertiaryContainer = Color(0xFF2E1065),
    onTertiaryContainer = Color(0xFFDDD6FE),

    error = LjRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = LjRedContainer,
    onErrorContainer = LjOnRedContainer,

    background = LjSurfaceDark,
    onBackground = LjOnSurfaceDark,

    surface = LjSurfaceDark,
    onSurface = LjOnSurfaceDark,
    surfaceVariant = LjSurfaceDarkVariant,
    onSurfaceVariant = LjOnSurfaceDarkVariant,

    outline = LjOutlineDark,
    outlineVariant = Color(0xFF334155),

    inverseSurface = LjSurfaceLight,
    inverseOnSurface = LjOnSurfaceLight,
    inversePrimary = LjBlueDark,

    scrim = Color(0x80000000),
)

val LjLightColorScheme = lightColorScheme(
    primary = LjBlueDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A5F),

    secondary = LjGreenDark,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF052E16),

    tertiary = Color(0xFF7C3AED),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF1E003F),

    error = LjRedDark,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF450A0A),

    background = LjSurfaceLight,
    onBackground = LjOnSurfaceLight,

    surface = LjSurfaceLight,
    onSurface = LjOnSurfaceLight,
    surfaceVariant = LjSurfaceLightVariant,
    onSurfaceVariant = LjOnSurfaceLightVariant,

    outline = LjOutlineLight,
    outlineVariant = Color(0xFFE2E8F0),

    inverseSurface = LjSurfaceDark,
    inverseOnSurface = LjOnSurfaceDark,
    inversePrimary = LjBlueLight,

    scrim = Color(0x80000000),
)
