package com.task.hotelhop.presentation.favorite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
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
import com.task.hotelhop.presentation.design_system.component.HotelCard
import com.task.hotelhop.presentation.design_system.component.HotelHopEmptyState
import com.task.hotelhop.presentation.design_system.component.HotelHopSnackbarHost
import com.task.hotelhop.presentation.design_system.component.LoginRequiredDialog
import com.task.hotelhop.presentation.design_system.component.UnfavoriteConfirmDialog
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoriteScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: FavoriteViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val colors = HotelHopTheme.colors

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is FavoriteUiEffect.NavigateToDetails -> onNavigateToDetails(effect.hotelId)
            FavoriteUiEffect.NavigateToLogin -> onNavigateToLogin()
            is FavoriteUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.favorites_title),
                style = HotelHopTheme.typography.headlineSmall,
                color = colors.textTitle
            )
            Spacer(modifier = Modifier.height(16.dp))
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                !uiState.isLoggedIn -> {
                    HotelHopEmptyState(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = stringResource(R.string.favorites_guest_title),
                        body = stringResource(R.string.favorites_guest_body),
                        actionLabel = stringResource(R.string.auth_required_action),
                        onAction = { viewModel.onEvent(FavoriteUiEvent.SignInClicked) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.hotels.isEmpty() -> {
                    HotelHopEmptyState(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = stringResource(R.string.favorites_empty_title),
                        body = stringResource(R.string.favorites_empty_body),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.hotels, key = { it.id }) { hotel ->
                            AnimatedVisibility(
                                visible = true,
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                HotelCard(
                                    hotel = hotel,
                                    onClick = { viewModel.onEvent(FavoriteUiEvent.HotelClicked(hotel.id)) },
                                    onFavoriteClick = { viewModel.onEvent(FavoriteUiEvent.FavoriteToggled(hotel)) }
                                )
                            }
                        }
                    }
                }
            }
        }
        HotelHopSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    LoginRequiredDialog(
        visible = uiState.showLoginRequired,
        onLogin = { viewModel.onEvent(FavoriteUiEvent.LoginRequiredConfirmed) },
        onDismiss = { viewModel.onEvent(FavoriteUiEvent.LoginRequiredDismissed) }
    )
    UnfavoriteConfirmDialog(
        visible = uiState.pendingUnfavorite != null,
        onConfirm = { viewModel.onEvent(FavoriteUiEvent.UnfavoriteConfirmed) },
        onDismiss = { viewModel.onEvent(FavoriteUiEvent.UnfavoriteDismissed) }
    )
}
