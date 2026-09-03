package com.task.hotelhop.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.usecase.hotel.GetFavoriteHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.GetPagedHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.SearchHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
import com.task.hotelhop.domain.usecase.user.CheckUserLoggedInUseCase
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
    private val getPagedHotelsUseCase: GetPagedHotelsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val checkUserLoggedInUseCase: CheckUserLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<SearchUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var favoriteIds: Set<String> = emptySet()
    private var cachedHotels: List<Hotel> = emptyList()
    private var lastRemoteResults: List<Hotel> = emptyList()

    init {
        viewModelScope.launch {
            checkUserLoggedInUseCase().collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            }
        }
        viewModelScope.launch {
            getFavoriteHotelsUseCase().collect { favorites ->
                favoriteIds = favorites.map { it.id }.toSet()
                applyCurrentResults()
            }
        }
        viewModelScope.launch {
            getPagedHotelsUseCase.observeHotels().collect { hotels ->
                cachedHotels = hotels.distinctBy { it.id }
                if (_uiState.value.query.isBlank()) applyCurrentResults()
            }
        }
        viewModelScope.launch {
            _uiState
                .map { Triple(it.query, it.priceFilter, it.ratingFilter) }
                .distinctUntilChanged()
                .debounce(400)
                .collect { (query, priceFilter, ratingFilter) ->
                    resolveResults(query.trim(), priceFilter, ratingFilter)
                }
        }
    }

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> _uiState.update { it.copy(query = event.query) }
            is SearchUiEvent.PriceFilterSelected -> _uiState.update {
                it.copy(priceFilter = if (it.priceFilter == event.filter) PriceFilter.ANY else event.filter)
            }
            is SearchUiEvent.RatingFilterSelected -> _uiState.update {
                it.copy(ratingFilter = if (it.ratingFilter == event.filter) RatingFilter.ANY else event.filter)
            }
            is SearchUiEvent.FavoriteToggled -> requestFavoriteToggle(event.hotel)
            is SearchUiEvent.HotelClicked -> viewModelScope.launch {
                _effect.send(SearchUiEffect.NavigateToDetails(event.hotelId))
            }
            SearchUiEvent.LoginRequiredConfirmed -> viewModelScope.launch {
                _uiState.update { it.copy(showLoginRequired = false) }
                _effect.send(SearchUiEffect.NavigateToLogin)
            }
            SearchUiEvent.LoginRequiredDismissed -> _uiState.update { it.copy(showLoginRequired = false) }
            SearchUiEvent.UnfavoriteConfirmed -> confirmUnfavorite()
            SearchUiEvent.UnfavoriteDismissed -> _uiState.update { it.copy(pendingUnfavorite = null) }
        }
    }

    private suspend fun resolveResults(query: String, priceFilter: PriceFilter, ratingFilter: RatingFilter) {
        if (query.isEmpty()) {
            lastRemoteResults = emptyList()
            if (priceFilter == PriceFilter.ANY && ratingFilter == RatingFilter.ANY) {
                _uiState.update { it.copy(isSearching = false, results = emptyList(), isEmpty = false) }
                return
            }
            val filtered = applyFilters(cachedHotels, priceFilter, ratingFilter)
            _uiState.update {
                it.copy(
                    isSearching = false,
                    results = filtered.withFavoriteFlags(),
                    isEmpty = filtered.isEmpty()
                )
            }
            return
        }

        _uiState.update { it.copy(isSearching = true, isEmpty = false) }
        runCatching { searchHotelsUseCase(query, SEARCH_LIMIT) }
            .onSuccess { results ->
                lastRemoteResults = results
                val filtered = applyFilters(results, priceFilter, ratingFilter)
                _uiState.update {
                    it.copy(
                        results = filtered.withFavoriteFlags(),
                        isSearching = false,
                        isEmpty = filtered.isEmpty()
                    )
                }
            }
            .onFailure { throwable ->
                val fallback = applyFilters(
                    cachedHotels.filter { hotel ->
                        hotel.name.contains(query, ignoreCase = true) ||
                            hotel.city.contains(query, ignoreCase = true)
                    },
                    priceFilter,
                    ratingFilter
                )
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        results = fallback.withFavoriteFlags(),
                        isEmpty = fallback.isEmpty()
                    )
                }
                if (fallback.isEmpty()) {
                    _effect.send(SearchUiEffect.ShowSnackbar(throwable.toUiText()))
                }
            }
    }

    private fun applyCurrentResults() {
        val state = _uiState.value
        if (state.query.isBlank() && !state.hasActiveFilters) {
            _uiState.update { it.copy(results = emptyList(), isEmpty = false) }
            return
        }
        val source = if (state.query.isBlank()) cachedHotels else lastRemoteResults.ifEmpty { state.results }
        val filtered = applyFilters(source, state.priceFilter, state.ratingFilter)
        _uiState.update {
            it.copy(
                results = filtered.withFavoriteFlags(),
                isEmpty = filtered.isEmpty()
            )
        }
    }

    private fun requestFavoriteToggle(hotel: Hotel) {
        if (!_uiState.value.isLoggedIn) {
            _uiState.update { it.copy(showLoginRequired = true) }
            return
        }
        if (hotel.isFavorite) {
            _uiState.update { it.copy(pendingUnfavorite = hotel) }
            return
        }
        toggleFavorite(hotel, favorite = true)
    }

    private fun confirmUnfavorite() {
        val hotel = _uiState.value.pendingUnfavorite ?: return
        _uiState.update { it.copy(pendingUnfavorite = null) }
        toggleFavorite(hotel, favorite = false)
    }

    private fun toggleFavorite(hotel: Hotel, favorite: Boolean) {
        viewModelScope.launch {
            runCatching { toggleFavoriteUseCase(hotel.id, favorite) }
                .onFailure { _effect.send(SearchUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }

    private fun List<Hotel>.withFavoriteFlags(): List<Hotel> {
        return map { it.copy(isFavorite = it.id in favoriteIds) }
    }

    private companion object {
        const val SEARCH_LIMIT = 30

        fun applyFilters(
            hotels: List<Hotel>,
            priceFilter: PriceFilter,
            ratingFilter: RatingFilter
        ): List<Hotel> {
            return hotels.filter { hotel ->
                matchesPrice(hotel, priceFilter) && matchesRating(hotel, ratingFilter)
            }
        }

        fun matchesPrice(hotel: Hotel, filter: PriceFilter): Boolean = when (filter) {
            PriceFilter.ANY -> true
            PriceFilter.BUDGET -> hotel.pricePerNight in 0.01..99.99
            PriceFilter.MID -> hotel.pricePerNight in 100.0..199.99
            PriceFilter.PREMIUM -> hotel.pricePerNight >= 200.0
        }

        fun matchesRating(hotel: Hotel, filter: RatingFilter): Boolean = when (filter) {
            RatingFilter.ANY -> true
            RatingFilter.THREE_PLUS -> hotel.rating >= 3.0
            RatingFilter.FOUR_PLUS -> hotel.rating >= 4.0
            RatingFilter.FOUR_FIVE_PLUS -> hotel.rating >= 4.5
        }
    }
}
