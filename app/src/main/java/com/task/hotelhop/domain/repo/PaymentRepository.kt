package com.task.hotelhop.domain.repo

import com.task.hotelhop.domain.entity.PaymobCheckoutSession
import com.task.hotelhop.domain.entity.User

interface PaymentRepository {
    suspend fun createCardCheckout(
        amountEgp: Double,
        hotelName: String,
        reference: String,
        user: User?
    ): PaymobCheckoutSession
}
