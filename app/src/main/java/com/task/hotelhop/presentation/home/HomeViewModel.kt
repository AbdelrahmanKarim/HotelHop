package com.task.hotelhop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.R
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.usecase.hotel.GetPagedHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
import com.task.hotelhop.domain.usecase.user.CheckUserLoggedInUseCase
import com.task.hotelhop.presentation.util.UiText
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getPagedHotelsUseCase: GetPagedHotelsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val checkUserLoggedInUseCase: CheckUserLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var offset = 0
    private var pagingJob: Job? = null

    init {
        observeAuth()
        observeCachedHotels()
        refresh(isInitial = true)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Refresh, HomeUiEvent.Retry -> refresh(isInitial = false)
            HomeUiEvent.LoadNextPage -> loadNextPage()
            is HomeUiEvent.FavoriteToggled -> requestFavoriteToggle(event.hotel)
            is HomeUiEvent.HotelClicked -> viewModelScope.launch {
                _effect.send(HomeUiEffect.NavigateToDetails(event.hotelId))
            }
            HomeUiEvent.PromoClicked -> viewModelScope.launch {
                _effect.send(HomeUiEffect.ShowSnackbar(UiText.StringResource(R.string.home_promo_snackbar)))
            }
            HomeUiEvent.LoginRequiredConfirmed -> viewModelScope.launch {
                _uiState.update { it.copy(showLoginRequired = false) }
                _effect.send(HomeUiEffect.NavigateToLogin)
            }
            HomeUiEvent.LoginRequiredDismissed -> _uiState.update { it.copy(showLoginRequired = false) }
            HomeUiEvent.UnfavoriteConfirmed -> confirmUnfavorite()
            HomeUiEvent.UnfavoriteDismissed -> _uiState.update { it.copy(pendingUnfavorite = null) }
        }
    }

    private fun observeAuth() {
        viewModelScope.launch {
            checkUserLoggedInUseCase().collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            }
        }
    }

    private fun observeCachedHotels() {
        viewModelScope.launch {
            getPagedHotelsUseCase.observeHotels().collect { hotels ->
                val uniqueHotels = hotels.distinctBy { it.id }
                _uiState.update { state ->
                    if (uniqueHotels.isEmpty() && state.hotels.isNotEmpty() &&
                        (state.isRefreshing || state.isLoading)
                    ) {
                        return@update state
                    }
                    val (popular, bestPrice) = featuredSections(uniqueHotels)
                    state.copy(
                        hotels = uniqueHotels,
                        popularHotels = popular,
                        bestPriceHotels = bestPrice,
                        isLoading = uniqueHotels.isEmpty() && state.isLoading,
                        isOfflineEmpty = false
                    )
                }
            }
        }
    }

    private fun refresh(isInitial: Boolean) {
        pagingJob?.cancel()
        pagingJob = viewModelScope.launch {
            offset = 0
            _uiState.update {
                it.copy(
                    isLoading = isInitial && it.hotels.isEmpty(),
                    isRefreshing = !isInitial || it.hotels.isNotEmpty(),
                    endReached = false,
                    isOfflineEmpty = false
                )
            }
            val result = runCatching { getPagedHotelsUseCase(PAGE_SIZE, 0) }
            result
                .onSuccess { loaded ->
                    offset = loaded
                    _uiState.update {
                        it.copy(
                            endReached = loaded < PAGE_SIZE,
                            isRefreshing = false,
                            isLoading = it.hotels.isEmpty() && loaded > 0
                        )
                    }
                }
                .onFailure { throwable ->
                    offset = _uiState.value.hotels.size
                    handlePagingError(throwable, isInitial = true)
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                }
        }
    }

    private fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || state.endReached || state.isLoading || pagingJob?.isActive == true) return
        pagingJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            runCatching { getPagedHotelsUseCase(PAGE_SIZE, offset) }
                .onSuccess { loaded ->
                    offset += loaded
                    _uiState.update { it.copy(endReached = loaded < PAGE_SIZE) }
                }
                .onFailure { handlePagingError(it, isInitial = false) }
            _uiState.update { it.copy(isLoadingMore = false) }
        }
    }

    private suspend fun handlePagingError(throwable: Throwable, isInitial: Boolean) {
        when (throwable) {
            is AppException.OfflineAndNoCacheException -> _uiState.update { it.copy(isOfflineEmpty = true) }
            else -> {
                if (isInitial && _uiState.value.hotels.isEmpty()) {
                    _uiState.update { it.copy(isOfflineEmpty = throwable is AppException.NetworkException) }
                }
                _effect.send(HomeUiEffect.ShowSnackbar(throwable.toUiText()))
            }
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
                .onFailure { _effect.send(HomeUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val FEATURED_COUNT = 8

        fun featuredSections(hotels: List<Hotel>): Pair<List<Hotel>, List<Hotel>> {
            val popular = hotels
                .sortedWith(compareByDescending<Hotel> { it.rating }.thenBy { it.id })
                .take(FEATURED_COUNT)
            val popularIds = popular.map { it.id }.toSet()
            val remaining = hotels.filter { it.id !in popularIds }
            val bestPrice = remaining
                .filter { it.pricePerNight > 0 }
                .sortedWith(compareBy<Hotel> { it.pricePerNight }.thenByDescending { it.rating }.thenBy { it.id })
                .take(FEATURED_COUNT)
            return popular to bestPrice
        }
    }
}
