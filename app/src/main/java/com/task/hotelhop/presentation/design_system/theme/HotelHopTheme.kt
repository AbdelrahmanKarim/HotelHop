package com.task.hotelhop.presentation.design_system.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import android.view.View
import com.task.hotelhop.presentation.design_system.color.HotelHopColors
import com.task.hotelhop.presentation.design_system.color.LocalHotelHopColors
import com.task.hotelhop.presentation.design_system.color.darkColors
import com.task.hotelhop.presentation.design_system.color.lightColors
import com.task.hotelhop.presentation.design_system.typography.HotelHopTypography
import com.task.hotelhop.presentation.design_system.typography.LocalHotelHopTypography

object HotelHopTheme {
    val colors: HotelHopColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHotelHopColors.current

    val typography: HotelHopTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalHotelHopTypography.current
}

@Composable
fun HotelHopTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) darkColors else lightColors
    val typography = HotelHopTypography()
    val layoutDir = if (LocalConfiguration.current.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDir,
        LocalHotelHopColors provides colors,
        LocalHotelHopTypography provides typography
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterial(isDarkTheme),
            typography = typography.toMaterial(),
            content = content
        )
    }
}

private fun HotelHopColors.toMaterial(isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryVariant,
            onPrimaryContainer = onPrimary,
            secondary = secondary,
            onSecondary = onPrimary,
            secondaryContainer = secondaryVariant,
            onSecondaryContainer = textTitle,
            background = surface,
            onBackground = textTitle,
            surface = surface,
            onSurface = textTitle,
            surfaceVariant = surfaceLow,
            onSurfaceVariant = textBody,
            surfaceContainerLowest = surface,
            surfaceContainerLow = surfaceLow,
            surfaceContainer = surfaceLow,
            surfaceContainerHigh = surfaceLow,
            surfaceContainerHighest = surfaceLow,
            outline = stroke,
            outlineVariant = stroke,
            error = error,
            onError = onPrimary,
            inverseSurface = textTitle,
            inverseOnSurface = surface
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryVariant,
            onPrimaryContainer = onPrimary,
            secondary = secondary,
            onSecondary = onPrimary,
            secondaryContainer = secondaryVariant,
            onSecondaryContainer = textTitle,
            background = surface,
            onBackground = textTitle,
            surface = surface,
            onSurface = textTitle,
            surfaceVariant = surfaceLow,
            onSurfaceVariant = textBody,
            surfaceContainerLowest = surface,
            surfaceContainerLow = surfaceLow,
            surfaceContainer = surfaceLow,
            surfaceContainerHigh = surfaceLow,
            surfaceContainerHighest = surfaceLow,
            outline = stroke,
            outlineVariant = stroke,
            error = error,
            onError = onPrimary,
            inverseSurface = textTitle,
            inverseOnSurface = surface
        )
    }
}

private fun HotelHopTypography.toMaterial(): Typography = Typography(
    displayLarge = headlineLarge,
    displayMedium = headlineMedium,
    displaySmall = headlineSmall,
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall
)
