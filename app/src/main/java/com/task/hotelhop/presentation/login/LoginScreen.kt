package com.task.hotelhop.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.task.hotelhop.R
import com.task.hotelhop.presentation.design_system.component.HotelHopButton
import com.task.hotelhop.presentation.design_system.component.HotelHopLoadingOverlay
import com.task.hotelhop.presentation.design_system.component.HotelHopOutlinedButton
import com.task.hotelhop.presentation.design_system.component.HotelHopSnackbarHost
import com.task.hotelhop.presentation.design_system.component.HotelHopTextField
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import com.task.hotelhop.presentation.util.findActivityOrNull
import com.task.hotelhop.presentation.util.UiText
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val colors = HotelHopTheme.colors

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            LoginUiEffect.NavigateToHome -> onNavigateToHome()
            LoginUiEffect.NavigateToRegister -> onNavigateToRegister()
            LoginUiEffect.LaunchGoogleSignIn -> {
                scope.launch {
                    val activity = context.findActivityOrNull()
                    if (activity == null) {
                        viewModel.onEvent(
                            LoginUiEvent.GoogleSignInFailed(UiText.StringResource(R.string.error_google_sign_in))
                        )
                    } else {
                        runCatching { GoogleAuthHelper.requestIdToken(activity) }
                            .onSuccess { token ->
                                viewModel.onEvent(LoginUiEvent.GoogleIdTokenReceived(token))
                            }
                            .onFailure { throwable ->
                                val message = if (throwable.message == "missing_web_client") {
                                    UiText.StringResource(R.string.error_google_not_configured)
                                } else {
                                    UiText.StringResource(R.string.error_google_sign_in)
                                }
                                viewModel.onEvent(LoginUiEvent.GoogleSignInFailed(message))
                            }
                    }
                }
            }
            is LoginUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = HotelHopTheme.typography.headlineLarge,
                color = colors.textTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = HotelHopTheme.typography.bodyMedium,
                color = colors.textBody
            )
            Spacer(modifier = Modifier.height(32.dp))
            HotelHopTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(LoginUiEvent.EmailChanged(it)) },
                label = stringResource(R.string.login_email),
                error = uiState.emailError,
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = colors.textHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HotelHopTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(LoginUiEvent.PasswordChanged(it)) },
                label = stringResource(R.string.login_password),
                error = uiState.passwordError,
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.textHint) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.onEvent(LoginUiEvent.TogglePasswordVisibility) }) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = stringResource(
                                if (uiState.isPasswordVisible) R.string.content_desc_hide_password
                                else R.string.content_desc_show_password
                            ),
                            tint = colors.textHint
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.onEvent(LoginUiEvent.Submit)
                })
            )
            Spacer(modifier = Modifier.height(24.dp))
            HotelHopButton(
                text = stringResource(R.string.login_action),
                onClick = { viewModel.onEvent(LoginUiEvent.Submit) },
                loading = uiState.isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))
            HotelHopOutlinedButton(
                text = stringResource(R.string.login_google),
                onClick = { viewModel.onEvent(LoginUiEvent.GoogleSignInClicked) },
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    style = HotelHopTheme.typography.bodySmall,
                    color = colors.textBody
                )
                TextButton(onClick = { viewModel.onEvent(LoginUiEvent.RegisterClicked) }) {
                    Text(
                        text = stringResource(R.string.login_register),
                        style = HotelHopTheme.typography.labelMedium,
                        color = colors.primary
                    )
                }
            }
        }
        HotelHopSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        HotelHopLoadingOverlay(visible = uiState.isLoading)
    }
}
