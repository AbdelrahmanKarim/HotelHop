package com.task.hotelhop.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
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
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val colors = HotelHopTheme.colors

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is SearchUiEffect.NavigateToDetails -> onNavigateToDetails(effect.hotelId)
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
            when {
                uiState.isSearching -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                uiState.query.isBlank() -> {
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
}
