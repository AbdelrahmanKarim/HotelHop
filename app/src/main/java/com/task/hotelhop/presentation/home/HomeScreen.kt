package com.task.hotelhop.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.task.hotelhop.R
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.design_system.component.HotelCard
import com.task.hotelhop.presentation.design_system.component.HotelHopEmptyState
import com.task.hotelhop.presentation.design_system.component.HotelHopSnackbarHost
import com.task.hotelhop.presentation.design_system.component.LoginRequiredDialog
import com.task.hotelhop.presentation.design_system.component.UnfavoriteConfirmDialog
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val colors = HotelHopTheme.colors
    val listState = rememberLazyListState()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is HomeUiEffect.NavigateToDetails -> onNavigateToDetails(effect.hotelId)
            HomeUiEffect.NavigateToLogin -> onNavigateToLogin()
            is HomeUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                val itemCount = listState.layoutInfo.totalItemsCount
                if (lastVisible != null && lastVisible >= itemCount - 3) {
                    viewModel.onEvent(HomeUiEvent.LoadNextPage)
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(HomeUiEvent.Refresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                uiState.isRefreshing && uiState.hotels.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                uiState.isOfflineEmpty -> {
                    HotelHopEmptyState(
                        icon = Icons.Outlined.WifiOff,
                        title = stringResource(R.string.home_offline_title),
                        body = stringResource(R.string.home_offline_body),
                        actionLabel = stringResource(R.string.retry),
                        onAction = { viewModel.onEvent(HomeUiEvent.Retry) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.hotels.isEmpty() -> {
                    HotelHopEmptyState(
                        icon = Icons.Outlined.LocalOffer,
                        title = stringResource(R.string.home_empty_title),
                        body = stringResource(R.string.home_empty_body),
                        actionLabel = stringResource(R.string.retry),
                        onAction = { viewModel.onEvent(HomeUiEvent.Retry) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.home_greeting),
                                style = HotelHopTheme.typography.headlineSmall,
                                color = colors.textTitle,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                            )
                        }
                        item {
                            PromoCard(
                                onClick = { viewModel.onEvent(HomeUiEvent.PromoClicked) },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                        if (uiState.popularHotels.isNotEmpty()) {
                            item {
                                HotelSection(
                                    title = stringResource(R.string.home_section_popular),
                                    hotels = uiState.popularHotels,
                                    onHotelClick = { viewModel.onEvent(HomeUiEvent.HotelClicked(it.id)) },
                                    onFavoriteClick = { viewModel.onEvent(HomeUiEvent.FavoriteToggled(it)) }
                                )
                            }
                        }
                        if (uiState.bestPriceHotels.isNotEmpty()) {
                            item {
                                HotelSection(
                                    title = stringResource(R.string.home_section_best_price),
                                    hotels = uiState.bestPriceHotels,
                                    onHotelClick = { viewModel.onEvent(HomeUiEvent.HotelClicked(it.id)) },
                                    onFavoriteClick = { viewModel.onEvent(HomeUiEvent.FavoriteToggled(it)) }
                                )
                            }
                        }
                        item {
                            Text(
                                text = stringResource(R.string.home_section_explore),
                                style = HotelHopTheme.typography.titleSmall,
                                color = colors.textTitle,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                            )
                        }
                        items(uiState.hotels.chunked(2)) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { hotel ->
                                    HotelCard(
                                        hotel = hotel,
                                        onClick = { viewModel.onEvent(HomeUiEvent.HotelClicked(hotel.id)) },
                                        onFavoriteClick = { viewModel.onEvent(HomeUiEvent.FavoriteToggled(hotel)) },
                                        modifier = Modifier.weight(1f),
                                        compact = true
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = colors.primary)
                                }
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
        onLogin = { viewModel.onEvent(HomeUiEvent.LoginRequiredConfirmed) },
        onDismiss = { viewModel.onEvent(HomeUiEvent.LoginRequiredDismissed) }
    )
    UnfavoriteConfirmDialog(
        visible = uiState.pendingUnfavorite != null,
        onConfirm = { viewModel.onEvent(HomeUiEvent.UnfavoriteConfirmed) },
        onDismiss = { viewModel.onEvent(HomeUiEvent.UnfavoriteDismissed) }
    )
}

@Composable
private fun PromoCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = HotelHopTheme.colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = colors.primary
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_promo_title),
                    style = HotelHopTheme.typography.titleMedium,
                    color = colors.onPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_promo_body),
                    style = HotelHopTheme.typography.bodySmall,
                    color = colors.onPrimary.copy(alpha = 0.86f)
                )
            }
            Icon(
                imageVector = Icons.Outlined.LocalOffer,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun HotelSection(
    title: String,
    hotels: List<Hotel>,
    onHotelClick: (Hotel) -> Unit,
    onFavoriteClick: (Hotel) -> Unit
) {
    val colors = HotelHopTheme.colors
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            text = title,
            style = HotelHopTheme.typography.titleSmall,
            color = colors.textTitle,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hotels, key = { it.id }) { hotel ->
                HotelCard(
                    hotel = hotel,
                    onClick = { onHotelClick(hotel) },
                    onFavoriteClick = { onFavoriteClick(hotel) },
                    compact = true,
                    modifier = Modifier.width(220.dp)
                )
            }
        }
    }
}
