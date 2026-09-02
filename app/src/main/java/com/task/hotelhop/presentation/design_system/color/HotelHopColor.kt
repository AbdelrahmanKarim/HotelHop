package com.task.hotelhop.presentation.design_system.color

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


val Primary = Color(0xFF39BADF)
val PrimaryVariant = Color(0xFF14A2CC)
val BlueAccent = Color(0xFF18A3FF)
val BlueVariantLight = Color(0xFFEBF7FF)
val BlueVariantDark = Color(0xFF0F2936)

val Secondary = Color(0xFFFF7C38)
val SecondaryVariantLight = Color(0xFFFFF2EB)
val SecondaryVariantDark = Color(0xFF3A251D)

// --- Bases ---
val BaseDark = Color(0xFF1F1F1F)
val BaseLight = Color(0xFFFFFFFF)

// --- Surfaces ---
val SurfaceLight = Color(0xFFF9FBFB)
val SurfaceLowLight = Color(0xFFFFFFFF)
val DisableLight = Color(0xFFE8EBED)

val SurfaceDark = Color(0xFF121212)
val SurfaceLowDark = Color(0xFF1E1E1E)
val DisableDark = Color(0xFF303030)

// --- Status Colors ---
val StatusYellow = Color(0xFFF5A623)
val StatusGreen = Color(0xFF51AC46)
val StatusRed = Color(0xFFF4505C)

// --- Color Data Class ---
data class HotelHopColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val textTitle: Color,
    val textBody: Color,
    val textHint: Color,
    val surface: Color,
    val surfaceLow: Color,
    val stroke: Color,
    val disable: Color,
    val onPrimary: Color,
    val error: Color
)

// --- Light Theme Palette ---
val lightColors = HotelHopColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    secondary = Secondary,
    secondaryVariant = SecondaryVariantLight,
    textTitle = BaseDark.copy(alpha = 0.87f),
    textBody = BaseDark.copy(alpha = 0.60f),
    textHint = BaseDark.copy(alpha = 0.38f),
    surface = SurfaceLight,
    surfaceLow = SurfaceLowLight,
    stroke = BaseDark.copy(alpha = 0.12f),
    disable = DisableLight,
    onPrimary = Color.White,
    error = StatusRed
)

// --- Dark Theme Palette ---
val darkColors = HotelHopColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    secondary = Secondary,
    secondaryVariant = SecondaryVariantDark,
    textTitle = BaseLight.copy(alpha = 0.87f),
    textBody = BaseLight.copy(alpha = 0.60f),
    textHint = BaseLight.copy(alpha = 0.38f),
    surface = SurfaceDark,
    surfaceLow = SurfaceLowDark,
    stroke = BaseLight.copy(alpha = 0.12f),
    disable = DisableDark,
    onPrimary = Color.White,
    error = StatusRed
)

internal val LocalHotelHopColors = staticCompositionLocalOf { lightColors }