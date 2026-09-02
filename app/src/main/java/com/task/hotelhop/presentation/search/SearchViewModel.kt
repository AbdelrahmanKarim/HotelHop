package com.task.hotelhop.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.usecase.hotel.GetFavoriteHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.SearchHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchHotelsUseCase: SearchHotelsUseCase,
    private val getFavoriteHotelsUseCase: GetFavoriteHotelsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<SearchUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var favoriteIds: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            getFavoriteHotelsUseCase().collect { favorites ->
                favoriteIds = favorites.map { it.id }.toSet()
                _uiState.update { state ->
                    state.copy(results = state.results.map { it.copy(isFavorite = it.id in favoriteIds) })
                }
            }
        }
        viewModelScope.launch {
            _uiState.map { it.query }
                .distinctUntilChanged()
                .debounce(400)
                .collect { query ->
                    val trimmed = query.trim()
                    if (trimmed.isEmpty()) {
                        _uiState.update { it.copy(isSearching = false, results = emptyList(), isEmpty = false) }
                    } else {
                        search(trimmed)
                    }
                }
        }
    }

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> _uiState.update { it.copy(query = event.query) }
            is SearchUiEvent.FavoriteToggled -> toggleFavorite(event.hotel)
            is SearchUiEvent.HotelClicked -> viewModelScope.launch {
                _effect.send(SearchUiEffect.NavigateToDetails(event.hotelId))
            }
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, isEmpty = false) }
            runCatching { searchHotelsUseCase(query) }
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            results = results.map { hotel -> hotel.copy(isFavorite = hotel.id in favoriteIds) },
                            isSearching = false,
                            isEmpty = results.isEmpty()
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isSearching = false, isEmpty = true, results = emptyList()) }
                    _effect.send(SearchUiEffect.ShowSnackbar(throwable.toUiText()))
                }
        }
    }

    private fun toggleFavorite(hotel: Hotel) {
        viewModelScope.launch {
            runCatching { toggleFavoriteUseCase(hotel.id, !hotel.isFavorite) }
                .onFailure { _effect.send(SearchUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }
}
