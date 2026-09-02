package com.task.hotelhop.presentation.design_system.typography


import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.task.hotelhop.R

val rubikFontFamily = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_semibold, FontWeight.SemiBold)
)

data class HotelHopTypography(
    // Headlines (SemiBold)
    val headlineLarge: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 42.sp),
    val headlineMedium: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 36.sp),
    val headlineSmall: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 30.sp),

    // Titles (Medium)
    val titleLarge: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 30.sp),
    val titleMedium: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 28.sp),
    val titleSmall: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),

    // Body (Regular)
    val bodyLarge: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp),
    val bodyMedium: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    val bodySmall: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),

    // Labels (Medium & Regular)
    val labelLarge: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    val labelMedium: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 22.sp),
    val labelSmall: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    val labelXSmall: TextStyle = TextStyle(fontFamily = rubikFontFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 14.sp)
)

internal val LocalHotelHopTypography = staticCompositionLocalOf { HotelHopTypography() }