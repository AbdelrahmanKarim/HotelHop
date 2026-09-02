package com.task.hotelhop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.R
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.usecase.hotel.GetPagedHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
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
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var offset = 0
    private var pagingJob: Job? = null

    init {
        observeCachedHotels()
        refresh(isInitial = true)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Refresh, HomeUiEvent.Retry -> refresh(isInitial = false)
            HomeUiEvent.LoadNextPage -> loadNextPage()
            is HomeUiEvent.FavoriteToggled -> toggleFavorite(event.hotel)
            is HomeUiEvent.HotelClicked -> viewModelScope.launch {
                _effect.send(HomeUiEffect.NavigateToDetails(event.hotelId))
            }
            HomeUiEvent.PromoClicked -> viewModelScope.launch {
                _effect.send(HomeUiEffect.ShowSnackbar(UiText.StringResource(R.string.home_promo_snackbar)))
            }
        }
    }

    private fun observeCachedHotels() {
        viewModelScope.launch {
            getPagedHotelsUseCase.observeHotels().collect { hotels ->
                _uiState.update { state ->
                    state.copy(
                        hotels = hotels,
                        popularHotels = hotels.sortedByDescending { it.rating }.take(8),
                        bestPriceHotels = hotels.filter { it.pricePerNight > 0 }.sortedBy { it.pricePerNight }.take(8),
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
            runCatching { getPagedHotelsUseCase(PAGE_SIZE, 0) }
                .onSuccess { loaded ->
                    offset = loaded
                    _uiState.update { it.copy(endReached = loaded < PAGE_SIZE) }
                }
                .onFailure { handlePagingError(it, isInitial = true) }
            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
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

    private fun toggleFavorite(hotel: Hotel) {
        viewModelScope.launch {
            runCatching { toggleFavoriteUseCase(hotel.id, !hotel.isFavorite) }
                .onFailure { _effect.send(HomeUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
