package com.task.hotelhop.domain.entity

data class PaymentSession(
    val clientSecret: String,
    val checkoutUrl: String,
    val reference: String
)
