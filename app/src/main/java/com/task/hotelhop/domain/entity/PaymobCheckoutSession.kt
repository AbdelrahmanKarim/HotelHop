package com.task.hotelhop.domain.entity

data class PaymobCheckoutSession(
    val clientSecret: String,
    val checkoutUrl: String,
    val reference: String
)
