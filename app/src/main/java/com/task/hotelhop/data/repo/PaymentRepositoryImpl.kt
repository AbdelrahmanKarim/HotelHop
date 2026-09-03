package com.task.hotelhop.data.repo

import com.task.hotelhop.BuildConfig
import com.task.hotelhop.data.remote.dto.paymob.PaymobBillingData
import com.task.hotelhop.data.remote.dto.paymob.PaymobCustomer
import com.task.hotelhop.data.remote.dto.paymob.PaymobIntentionRequest
import com.task.hotelhop.data.remote.dto.paymob.PaymobItem
import com.task.hotelhop.data.remote.service.PaymobApiService
import com.task.hotelhop.domain.entity.PaymobCheckoutSession
import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.repo.PaymentRepository
import kotlin.math.roundToInt

class PaymentRepositoryImpl(
    private val paymobApi: PaymobApiService
) : PaymentRepository {

    override suspend fun createCardCheckout(
        amountEgp: Double,
        hotelName: String,
        reference: String,
        user: User?
    ): PaymobCheckoutSession {
        val amountCents = (amountEgp * 100).roundToInt().coerceAtLeast(100)
        val firstName = user?.firstName?.ifBlank { "Guest" } ?: "Guest"
        val lastName = user?.lastName?.ifBlank { "User" } ?: "User"
        val email = user?.email?.ifBlank { "guest@hotelhop.app" } ?: "guest@hotelhop.app"
        val response = runCatching {
            paymobApi.createIntention(
                PaymobIntentionRequest(
                    amount = amountCents,
                    currency = BuildConfig.PAYMOB_CURRENCY,
                    paymentMethods = listOf(BuildConfig.PAYMOB_INTEGRATION_ID),
                    items = listOf(
                        PaymobItem(
                            name = hotelName,
                            amount = amountCents,
                            description = "Hotel Hop simulated booking",
                            quantity = 1
                        )
                    ),
                    billingData = PaymobBillingData(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = PLACEHOLDER_PHONE
                    ),
                    customer = PaymobCustomer(
                        firstName = firstName,
                        lastName = lastName,
                        email = email
                    ),
                    specialReference = reference,
                    expiration = 3600,
                    redirectionUrl = REDIRECT_URI
                )
            )
        }.getOrElse { throw AppException.ServerException() }

        if (response.clientSecret.isBlank()) throw AppException.ServerException()
        val checkoutUrl =
            "${BuildConfig.PAYMOB_BASE_URL}/unifiedcheckout/?publicKey=${BuildConfig.PAYMOB_PUBLIC_KEY}&clientSecret=${response.clientSecret}"
        return PaymobCheckoutSession(
            clientSecret = response.clientSecret,
            checkoutUrl = checkoutUrl,
            reference = reference
        )
    }

    companion object {
        const val REDIRECT_URI = "https://hotelhop.app/paymob/result"
        private const val PLACEHOLDER_PHONE = "NA"
    }
}
