package com.task.hotelhop.presentation.checkout

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.util.UiText

enum class PaymentMethod { CASH, CARD }

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val checkInMillis: Long? = null,
    val checkOutMillis: Long? = null,
    val roomCount: Int = 1,
    val nights: Int = 0,
    val subtotal: Double = 0.0,
    val vat: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val isSubmitting: Boolean = false,
    val bookingReference: String? = null,
    val dateError: UiText? = null
)

sealed interface CheckoutUiEvent {
    data class CheckInSelected(val millis: Long) : CheckoutUiEvent
    data class CheckOutSelected(val millis: Long) : CheckoutUiEvent
    data object IncrementRooms : CheckoutUiEvent
    data object DecrementRooms : CheckoutUiEvent
    data class PaymentMethodSelected(val method: PaymentMethod) : CheckoutUiEvent
    data object ConfirmBooking : CheckoutUiEvent
    data class CardPaymentFinished(val success: Boolean) : CheckoutUiEvent
    data object DismissSuccess : CheckoutUiEvent
    data object BackClicked : CheckoutUiEvent
}

sealed interface CheckoutUiEffect {
    data object NavigateBack : CheckoutUiEffect
    data class LaunchPaymob(val checkoutUrl: String) : CheckoutUiEffect
    data class ShowSnackbar(val message: UiText) : CheckoutUiEffect
}
