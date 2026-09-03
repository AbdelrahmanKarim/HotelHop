package com.task.hotelhop.domain.usecase.payment

import com.task.hotelhop.domain.entity.PaymobCheckoutSession
import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.repo.PaymentRepository

class CreateCardPaymentUseCase(private val repository: PaymentRepository) {
    suspend operator fun invoke(
        amountEgp: Double,
        hotelName: String,
        reference: String,
        user: User?
    ): PaymobCheckoutSession {
        return repository.createCardCheckout(amountEgp, hotelName, reference, user)
    }
}
