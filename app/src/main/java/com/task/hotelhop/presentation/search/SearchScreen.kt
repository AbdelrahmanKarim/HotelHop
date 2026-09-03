package com.task.hotelhop.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.task.hotelhop.presentation.design_system.component.HotelHopTextField
import com.task.hotelhop.presentation.design_system.component.LoginRequiredDialog
import com.task.hotelhop.presentation.design_system.component.UnfavoriteConfirmDialog
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val colors = HotelHopTheme.colors

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is SearchUiEffect.NavigateToDetails -> onNavigateToDetails(effect.hotelId)
            SearchUiEffect.NavigateToLogin -> onNavigateToLogin()
            is SearchUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.nav_search),
                style = HotelHopTheme.typography.headlineSmall,
                color = colors.textTitle,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            HotelHopTextField(
                value = uiState.query,
                onValueChange = { viewModel.onEvent(SearchUiEvent.QueryChanged(it)) },
                label = stringResource(R.string.home_search_hint),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textHint) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.search_filter_price),
                style = HotelHopTheme.typography.labelMedium,
                color = colors.textBody,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilterChipRow {
                SearchFilterChip(
                    label = stringResource(R.string.search_filter_price_budget),
                    selected = uiState.priceFilter == PriceFilter.BUDGET,
                    onClick = { viewModel.onEvent(SearchUiEvent.PriceFilterSelected(PriceFilter.BUDGET)) }
                )
                SearchFilterChip(
                    label = stringResource(R.string.search_filter_price_mid),
                    selected = uiState.priceFilter == PriceFilter.MID,
                    onClick = { viewModel.onEvent(SearchUiEvent.PriceFilterSelected(PriceFilter.MID)) }
                )
                SearchFilterChip(
                    label = stringResource(R.string.search_filter_price_premium),
                    selected = uiState.priceFilter == PriceFilter.PREMIUM,
                    onClick = { viewModel.onEvent(SearchUiEvent.PriceFilterSelected(PriceFilter.PREMIUM)) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.search_filter_rating),
                style = HotelHopTheme.typography.labelMedium,
                color = colors.textBody,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilterChipRow {
                SearchFilterChip(
                    label = stringResource(R.string.search_filter_rating_3),
                    selected = uiState.ratingFilter == RatingFilter.THREE_PLUS,
                    onClick = { viewModel.onEvent(SearchUiEvent.RatingFilterSelected(RatingFilter.THREE_PLUS)) }
                )
                SearchFilterChip(
                    label = stringResource(R.string.search_filter_rating_4),
                    selected = uiState.ratingFilter == RatingFilter.FOUR_PLUS,
                    onClick = { viewModel.onEvent(SearchUiEvent.RatingFilterSelected(RatingFilter.FOUR_PLUS)) }
                )
                SearchFilterChip(
                    label = stringResource(R.string.search_filter_rating_45),
                    selected = uiState.ratingFilter == RatingFilter.FOUR_FIVE_PLUS,
                    onClick = { viewModel.onEvent(SearchUiEvent.RatingFilterSelected(RatingFilter.FOUR_FIVE_PLUS)) }
                )
            }
            when {
                uiState.isSearching -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                uiState.isIdle -> {
                    HotelHopEmptyState(
                        icon = Icons.Outlined.Search,
                        title = stringResource(R.string.search_idle_title),
                        body = stringResource(R.string.search_idle_body),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.isEmpty -> {
                    HotelHopEmptyState(
                        icon = Icons.Outlined.Search,
                        title = stringResource(R.string.home_search_empty_title),
                        body = stringResource(R.string.home_search_empty_body),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.results, key = { it.id }) { hotel ->
                            HotelCard(
                                hotel = hotel,
                                onClick = { viewModel.onEvent(SearchUiEvent.HotelClicked(hotel.id)) },
                                onFavoriteClick = { viewModel.onEvent(SearchUiEvent.FavoriteToggled(hotel)) }
                            )
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
        onLogin = { viewModel.onEvent(SearchUiEvent.LoginRequiredConfirmed) },
        onDismiss = { viewModel.onEvent(SearchUiEvent.LoginRequiredDismissed) }
    )
    UnfavoriteConfirmDialog(
        visible = uiState.pendingUnfavorite != null,
        onConfirm = { viewModel.onEvent(SearchUiEvent.UnfavoriteConfirmed) },
        onDismiss = { viewModel.onEvent(SearchUiEvent.UnfavoriteDismissed) }
    )
}

@Composable
private fun FilterChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() }
    )
}

@Composable
private fun SearchFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
