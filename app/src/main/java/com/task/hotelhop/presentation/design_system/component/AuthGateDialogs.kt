package com.task.hotelhop.presentation.design_system.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.task.hotelhop.R

@Composable
fun LoginRequiredDialog(
    visible: Boolean,
    onLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    HotelHopAlertDialog(
        title = stringResource(R.string.auth_required_title),
        body = stringResource(R.string.auth_required_body),
        confirmLabel = stringResource(R.string.auth_required_action),
        dismissLabel = stringResource(R.string.cancel),
        onConfirm = onLogin,
        onDismiss = onDismiss
    )
}

@Composable
fun UnfavoriteConfirmDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    HotelHopAlertDialog(
        title = stringResource(R.string.unfavorite_confirm_title),
        body = stringResource(R.string.unfavorite_confirm_body),
        confirmLabel = stringResource(R.string.unfavorite_confirm_action),
        dismissLabel = stringResource(R.string.cancel),
        confirmIsDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
