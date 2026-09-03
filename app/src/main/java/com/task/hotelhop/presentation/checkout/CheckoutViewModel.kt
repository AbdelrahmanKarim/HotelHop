package com.task.hotelhop.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.R
import com.task.hotelhop.domain.booking.BookingCalculator
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.usecase.hotel.GetHotelDetailsUseCase
import com.task.hotelhop.domain.usecase.payment.CreateCardPaymentUseCase
import com.task.hotelhop.domain.usecase.user.GetUserDetailsUseCase
import com.task.hotelhop.presentation.navigation.Screen
import com.task.hotelhop.presentation.util.UiText
import com.task.hotelhop.presentation.util.startOfTodayUtc
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CheckoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHotelDetailsUseCase: GetHotelDetailsUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val createCardPaymentUseCase: CreateCardPaymentUseCase
) : ViewModel() {

    private val hotelId: String = checkNotNull(savedStateHandle[Screen.Checkout.ARG_HOTEL_ID])
    private var pendingCardReference: String? = null

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<CheckoutUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadHotel()
    }

    fun onEvent(event: CheckoutUiEvent) {
        when (event) {
            is CheckoutUiEvent.CheckInSelected -> updateDates(checkIn = event.millis, checkOut = _uiState.value.checkOutMillis)
            is CheckoutUiEvent.CheckOutSelected -> updateDates(checkIn = _uiState.value.checkInMillis, checkOut = event.millis)
            CheckoutUiEvent.IncrementRooms -> updateRooms(_uiState.value.roomCount + 1)
            CheckoutUiEvent.DecrementRooms -> updateRooms(_uiState.value.roomCount - 1)
            is CheckoutUiEvent.PaymentMethodSelected -> _uiState.update { it.copy(paymentMethod = event.method) }
            CheckoutUiEvent.ConfirmBooking -> confirmBooking()
            is CheckoutUiEvent.CardPaymentFinished -> onCardResult(event.success)
            CheckoutUiEvent.DismissSuccess -> viewModelScope.launch { _effect.send(CheckoutUiEffect.NavigateBack) }
            CheckoutUiEvent.BackClicked -> viewModelScope.launch { _effect.send(CheckoutUiEffect.NavigateBack) }
        }
    }

    private fun loadHotel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { getHotelDetailsUseCase(hotelId) }
                .onSuccess { hotel ->
                    _uiState.update { state -> recalculate(state.copy(isLoading = false, hotel = hotel)) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(CheckoutUiEffect.ShowSnackbar(throwable.toUiText()))
                }
        }
    }

    private fun updateDates(checkIn: Long?, checkOut: Long?) {
        val adjustedCheckOut = if (checkIn != null && checkOut != null && checkOut <= checkIn) {
            null
        } else {
            checkOut
        }
        _uiState.update { state ->
            recalculate(
                state.copy(
                    checkInMillis = checkIn,
                    checkOutMillis = adjustedCheckOut,
                    dateError = dateError(checkIn, adjustedCheckOut)
                )
            )
        }
    }

    private fun updateRooms(count: Int) {
        _uiState.update { recalculate(it.copy(roomCount = BookingCalculator.coerceRooms(count))) }
    }

    private fun confirmBooking() {
        val state = _uiState.value
        val error = dateError(state.checkInMillis, state.checkOutMillis)
            ?: if (state.checkInMillis == null || state.checkOutMillis == null) {
                AppException.InvalidBookingDateException().toUiText()
            } else {
                null
            }
        when {
            error != null -> _uiState.update { it.copy(dateError = error) }
            state.roomCount < 1 -> viewModelScope.launch {
                _effect.send(CheckoutUiEffect.ShowSnackbar(AppException.InvalidRoomCountException().toUiText()))
            }
            state.paymentMethod == PaymentMethod.CASH -> {
                _uiState.update { it.copy(bookingReference = newReference()) }
            }
            else -> startCardPayment()
        }
    }

    private fun startCardPayment() {
        val state = _uiState.value
        val hotel = state.hotel ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val reference = newReference()
            pendingCardReference = reference
            val user = runCatching { getUserDetailsUseCase() }.getOrNull()
            runCatching {
                createCardPaymentUseCase(
                    amountEgp = state.total,
                    hotelName = hotel.name,
                    reference = reference,
                    user = user
                )
            }.onSuccess { session ->
                _effect.send(CheckoutUiEffect.LaunchPaymob(session.checkoutUrl))
            }.onFailure { throwable ->
                pendingCardReference = null
                _effect.send(CheckoutUiEffect.ShowSnackbar(throwable.toUiText()))
            }
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    private fun onCardResult(success: Boolean) {
        viewModelScope.launch {
            if (success) {
                _uiState.update { it.copy(bookingReference = pendingCardReference ?: newReference()) }
            } else {
                _effect.send(CheckoutUiEffect.ShowSnackbar(UiText.StringResource(R.string.checkout_payment_failed)))
            }
            pendingCardReference = null
        }
    }

    private fun recalculate(state: CheckoutUiState): CheckoutUiState {
        val nights = BookingCalculator.nights(state.checkInMillis, state.checkOutMillis)
        val quote = BookingCalculator.quote(
            pricePerNight = state.hotel?.pricePerNight ?: 0.0,
            nights = nights,
            roomCount = state.roomCount
        )
        return state.copy(
            nights = quote.nights,
            subtotal = quote.subtotal,
            vat = quote.vat,
            total = quote.total
        )
    }

    private fun newReference(): String = "HH-" + UUID.randomUUID().toString().take(8).uppercase()

    private companion object {
        fun dateError(checkIn: Long?, checkOut: Long?): UiText? {
            return BookingCalculator.dateViolation(checkIn, checkOut, startOfTodayUtc())?.toUiText()
        }
    }
}
