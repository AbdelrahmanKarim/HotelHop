package com.task.hotelhop.presentation.design_system.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
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

    CompositionLocalProvider(
        LocalHotelHopColors provides colors,
        LocalHotelHopTypography provides HotelHopTypography()
    ) {
        MaterialTheme(
            content = content
        )
    }
}