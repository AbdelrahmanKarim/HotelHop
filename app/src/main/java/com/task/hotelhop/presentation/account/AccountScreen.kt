package com.task.hotelhop.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.task.hotelhop.R
import com.task.hotelhop.presentation.design_system.component.HotelHopAlertDialog
import com.task.hotelhop.presentation.design_system.component.HotelHopButton
import com.task.hotelhop.presentation.design_system.component.HotelHopSnackbarHost
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import com.task.hotelhop.presentation.util.applyAppLanguage
import com.task.hotelhop.presentation.util.findActivityOrNull
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountScreen(
    onLoggedOut: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val colors = HotelHopTheme.colors

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            AccountUiEffect.NavigateToLogin -> onNavigateToLogin()
            AccountUiEffect.LoggedOut -> onLoggedOut()
            is AccountUiEffect.RecreateForLanguage -> {
                context.findActivityOrNull()?.applyAppLanguage(effect.languageCode)
            }
            is AccountUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.account_title),
                    style = HotelHopTheme.typography.headlineSmall,
                    color = colors.textTitle
                )
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surfaceLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = colors.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        Column {
                            val user = uiState.user
                            val composedName = listOfNotNull(user?.firstName, user?.lastName)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            val displayName = composedName
                                .ifBlank { user?.email?.substringBefore("@").orEmpty() }
                                .ifBlank { stringResource(R.string.account_guest) }
                            Text(displayName, style = HotelHopTheme.typography.titleSmall, color = colors.textTitle)
                            Text(
                                text = uiState.user?.email.orEmpty(),
                                style = HotelHopTheme.typography.bodySmall,
                                color = colors.textBody
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.account_theme),
                    style = HotelHopTheme.typography.titleSmall,
                    color = colors.textTitle
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip(
                        label = stringResource(R.string.account_theme_light),
                        selected = uiState.themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.onEvent(AccountUiEvent.ThemeSelected(ThemeMode.LIGHT)) }
                    )
                    ThemeChip(
                        label = stringResource(R.string.account_theme_dark),
                        selected = uiState.themeMode == ThemeMode.DARK,
                        onClick = { viewModel.onEvent(AccountUiEvent.ThemeSelected(ThemeMode.DARK)) }
                    )
                    ThemeChip(
                        label = stringResource(R.string.account_theme_system),
                        selected = uiState.themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.onEvent(AccountUiEvent.ThemeSelected(ThemeMode.SYSTEM)) }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.account_language),
                    style = HotelHopTheme.typography.titleSmall,
                    color = colors.textTitle
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip(
                        label = stringResource(R.string.account_language_english),
                        selected = uiState.languageCode == "en",
                        onClick = { viewModel.onEvent(AccountUiEvent.LanguageSelected("en")) }
                    )
                    ThemeChip(
                        label = stringResource(R.string.account_language_arabic),
                        selected = uiState.languageCode == "ar",
                        onClick = { viewModel.onEvent(AccountUiEvent.LanguageSelected("ar")) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (uiState.isLoggedIn) {
                    HotelHopButton(
                        text = stringResource(R.string.account_logout),
                        onClick = { viewModel.onEvent(AccountUiEvent.LogoutClicked) }
                    )
                } else {
                    HotelHopButton(
                        text = stringResource(R.string.account_sign_in),
                        onClick = { viewModel.onEvent(AccountUiEvent.SignInClicked) }
                    )
                }
            }
        }
        HotelHopSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (uiState.showLogoutConfirm) {
        HotelHopAlertDialog(
            title = stringResource(R.string.account_logout_confirm_title),
            body = stringResource(R.string.account_logout_confirm_body),
            confirmLabel = stringResource(R.string.account_logout),
            dismissLabel = stringResource(R.string.cancel),
            confirmIsDestructive = true,
            onConfirm = { viewModel.onEvent(AccountUiEvent.LogoutConfirmed) },
            onDismiss = { viewModel.onEvent(AccountUiEvent.LogoutDismissed) }
        )
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = HotelHopTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = HotelHopTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.primary.copy(alpha = 0.16f),
            selectedLabelColor = colors.primary,
            containerColor = colors.surfaceLow,
            labelColor = colors.textBody
        )
    )
}
