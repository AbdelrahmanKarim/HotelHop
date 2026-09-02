package com.task.hotelhop.presentation.design_system.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme

@Composable
fun HotelHopAlertDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String? = null,
    confirmIsDestructive: Boolean = false
) {
    val colors = HotelHopTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = HotelHopTheme.typography.titleMedium, color = colors.textTitle)
        },
        text = {
            Text(text = body, style = HotelHopTheme.typography.bodyMedium, color = colors.textBody)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    style = HotelHopTheme.typography.labelLarge,
                    color = if (confirmIsDestructive) colors.error else colors.primary
                )
            }
        },
        dismissButton = dismissLabel?.let {
            {
                TextButton(onClick = onDismiss) {
                    Text(text = it, style = HotelHopTheme.typography.labelLarge, color = colors.textBody)
                }
            }
        },
        containerColor = colors.surfaceLow,
        titleContentColor = colors.textTitle,
        textContentColor = colors.textBody
    )
}
