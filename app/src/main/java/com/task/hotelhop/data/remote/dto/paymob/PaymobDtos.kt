package com.task.hotelhop.data.remote.dto.paymob

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymobIntentionRequest(
    val amount: Int,
    val currency: String,
    @SerialName("payment_methods") val paymentMethods: List<Int>,
    val items: List<PaymobItem>,
    @SerialName("billing_data") val billingData: PaymobBillingData,
    val customer: PaymobCustomer,
    @SerialName("special_reference") val specialReference: String,
    val expiration: Int,
    @SerialName("redirection_url") val redirectionUrl: String
)

@Serializable
data class PaymobItem(
    val name: String,
    val amount: Int,
    val description: String,
    val quantity: Int
)

@Serializable
data class PaymobBillingData(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String,
    @SerialName("phone_number") val phoneNumber: String,
    val street: String = "NA",
    val building: String = "NA",
    val floor: String = "NA",
    val apartment: String = "NA",
    val city: String = "Cairo",
    val country: String = "EGY",
    val state: String = "Cairo",
    @SerialName("shipping_method") val shippingMethod: String = "NA",
    @SerialName("postal_code") val postalCode: String = "NA"
)

@Serializable
data class PaymobCustomer(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String
)

@Serializable
data class PaymobIntentionResponse(
    val id: String? = null,
    @SerialName("client_secret") val clientSecret: String = "",
    val status: String? = null
)
