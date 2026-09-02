package com.task.hotelhop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HotelDto(
    val id: String,
    val name: String,
    val city: String,
    val hotelDescription: String? = null,
    val main_photo: String? = null,
    val stars: Int? = null,
    val rating: Double? = null
)