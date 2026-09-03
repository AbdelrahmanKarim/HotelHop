package com.task.hotelhop.presentation.register

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.task.hotelhop.R
import com.task.hotelhop.presentation.design_system.component.HotelHopButton
import com.task.hotelhop.presentation.design_system.component.HotelHopLoadingOverlay
import com.task.hotelhop.presentation.design_system.component.HotelHopSnackbarHost
import com.task.hotelhop.presentation.design_system.component.HotelHopTextField
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val colors = HotelHopTheme.colors

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            RegisterUiEffect.NavigateToHome -> onNavigateToHome()
            RegisterUiEffect.NavigateToLogin -> onNavigateToLogin()
            is RegisterUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    val genderChoices = listOf(
        GenderOptions.MALE to stringResource(R.string.register_gender_male),
        GenderOptions.FEMALE to stringResource(R.string.register_gender_female),
        GenderOptions.UNSPECIFIED to stringResource(R.string.register_gender_unspecified)
    )

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
                text = stringResource(R.string.register_title),
                style = HotelHopTheme.typography.headlineLarge,
                color = colors.textTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.register_subtitle),
                style = HotelHopTheme.typography.bodyMedium,
                color = colors.textBody
            )
            Spacer(modifier = Modifier.height(24.dp))
            HotelHopTextField(
                value = uiState.firstName,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.FirstNameChanged(it)) },
                label = stringResource(R.string.register_first_name),
                error = uiState.firstNameError,
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = colors.textHint) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HotelHopTextField(
                value = uiState.lastName,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.LastNameChanged(it)) },
                label = stringResource(R.string.register_last_name),
                error = uiState.lastNameError,
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = colors.textHint) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HotelHopTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.EmailChanged(it)) },
                label = stringResource(R.string.login_email),
                error = uiState.emailError,
                leadingIcon = { Icon(Icons.Outlined.MailOutline, contentDescription = null, tint = colors.textHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HotelHopTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.PasswordChanged(it)) },
                label = stringResource(R.string.login_password),
                error = uiState.passwordError,
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.textHint) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.onEvent(RegisterUiEvent.TogglePasswordVisibility) }) {
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.register_gender),
                style = HotelHopTheme.typography.labelLarge,
                color = colors.textTitle
            )
            Column(modifier = Modifier.selectableGroup()) {
                genderChoices.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = uiState.gender == value,
                                onClick = { viewModel.onEvent(RegisterUiEvent.GenderChanged(value)) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.gender == value,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                        )
                        Text(
                            text = label,
                            style = HotelHopTheme.typography.bodyMedium,
                            color = colors.textTitle,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            uiState.genderError?.let {
                Text(
                    text = it.asString(),
                    color = colors.error,
                    style = HotelHopTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            HotelHopButton(
                text = stringResource(R.string.register_action),
                onClick = { viewModel.onEvent(RegisterUiEvent.Submit) },
                loading = uiState.isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.register_have_account),
                    style = HotelHopTheme.typography.bodySmall,
                    color = colors.textBody
                )
                TextButton(onClick = { viewModel.onEvent(RegisterUiEvent.LoginClicked) }) {
                    Text(
                        text = stringResource(R.string.register_login),
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
