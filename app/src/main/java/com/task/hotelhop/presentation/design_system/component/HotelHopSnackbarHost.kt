package com.task.hotelhop.presentation.design_system.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme

@Composable
fun HotelHopSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val colors = HotelHopTheme.colors

    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data, // Pass the data directly here
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.surfaceLow,
            contentColor = colors.textTitle,
            actionColor = colors.primary,
            modifier = Modifier.padding(12.dp)
        )
    }
}