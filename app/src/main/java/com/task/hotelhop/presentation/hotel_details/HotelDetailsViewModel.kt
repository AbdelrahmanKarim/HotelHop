package com.task.hotelhop.presentation.hotel_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.R
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.usecase.hotel.GetFavoriteHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.GetHotelDetailsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
import com.task.hotelhop.domain.usecase.user.CheckUserLoggedInUseCase
import com.task.hotelhop.presentation.navigation.Screen
import com.task.hotelhop.presentation.util.UiText
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HotelDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHotelDetailsUseCase: GetHotelDetailsUseCase,
    private val getFavoriteHotelsUseCase: GetFavoriteHotelsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val checkUserLoggedInUseCase: CheckUserLoggedInUseCase
) : ViewModel() {

    private val hotelId: String = checkNotNull(savedStateHandle[Screen.HotelDetails.ARG_HOTEL_ID])

    private val _uiState = MutableStateFlow(HotelDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HotelDetailsUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeAuth()
        loadDetails()
        observeFavoriteStatus()
    }

    private fun observeAuth() {
        viewModelScope.launch {
            checkUserLoggedInUseCase().collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            getFavoriteHotelsUseCase().collect { favorites ->
                val isFavorite = favorites.any { it.id == hotelId }
                _uiState.update { state ->
                    state.copy(hotel = state.hotel?.copy(isFavorite = isFavorite))
                }
            }
        }
    }

    fun onEvent(event: HotelDetailsUiEvent) {
        when (event) {
            HotelDetailsUiEvent.Retry -> loadDetails()
            HotelDetailsUiEvent.FavoriteToggled -> requestFavoriteToggle()
            HotelDetailsUiEvent.ViewOnMapClicked -> openMap()
            HotelDetailsUiEvent.BookClicked -> requestBooking()
            HotelDetailsUiEvent.BackClicked -> viewModelScope.launch {
                _effect.send(HotelDetailsUiEffect.NavigateBack)
            }
            HotelDetailsUiEvent.LoginRequiredConfirmed -> viewModelScope.launch {
                _uiState.update { it.copy(showLoginRequired = false) }
                _effect.send(HotelDetailsUiEffect.NavigateToLogin)
            }
            HotelDetailsUiEvent.LoginRequiredDismissed -> _uiState.update { it.copy(showLoginRequired = false) }
            HotelDetailsUiEvent.UnfavoriteConfirmed -> confirmUnfavorite()
            HotelDetailsUiEvent.UnfavoriteDismissed -> _uiState.update { it.copy(showUnfavoriteConfirm = false) }
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isOfflineEmpty = false) }
            runCatching { getHotelDetailsUseCase(hotelId) }
                .onSuccess { hotel ->
                    _uiState.update { it.copy(isLoading = false, hotel = hotel, isOfflineEmpty = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOfflineEmpty = throwable is AppException.NetworkException ||
                                throwable is AppException.OfflineAndNoCacheException
                        )
                    }
                    _effect.send(HotelDetailsUiEffect.ShowSnackbar(throwable.toUiText()))
                }
        }
    }

    private fun requestFavoriteToggle() {
        val hotel = _uiState.value.hotel ?: return
        if (!_uiState.value.isLoggedIn) {
            _uiState.update { it.copy(showLoginRequired = true) }
            return
        }
        if (hotel.isFavorite) {
            _uiState.update { it.copy(showUnfavoriteConfirm = true) }
            return
        }
        toggleFavorite(favorite = true)
    }

    private fun requestBooking() {
        if (!_uiState.value.isLoggedIn) {
            _uiState.update { it.copy(showLoginRequired = true) }
            return
        }
        viewModelScope.launch {
            _effect.send(HotelDetailsUiEffect.NavigateToCheckout(hotelId))
        }
    }

    private fun confirmUnfavorite() {
        _uiState.update { it.copy(showUnfavoriteConfirm = false) }
        toggleFavorite(favorite = false)
    }

    private fun toggleFavorite(favorite: Boolean) {
        val hotel = _uiState.value.hotel ?: return
        viewModelScope.launch {
            runCatching { toggleFavoriteUseCase(hotel.id, favorite) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(hotel = state.hotel?.copy(isFavorite = favorite))
                    }
                }
                .onFailure { _effect.send(HotelDetailsUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }

    private fun openMap() {
        viewModelScope.launch {
            val url = _uiState.value.hotel?.locationUrl.orEmpty()
            if (url.isBlank()) {
                _effect.send(HotelDetailsUiEffect.ShowSnackbar(UiText.StringResource(R.string.error_map_unavailable)))
            } else {
                _effect.send(HotelDetailsUiEffect.OpenMap(url))
            }
        }
    }
}
