package com.task.hotelhop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SemanticHotelDto(
    val id: String,
    val name: String,
    val city: String,
    val main_photo: String? = null,
    val address: String? = null,
    val score: Double? = null
)