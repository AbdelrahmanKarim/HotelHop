package com.task.hotelhop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HotelDetailsDto(
    val id: String,
    val name: String,
    val city: String,
    val address: String? = null,
    val hotelDescription: String? = null,
    val main_photo: String? = null,
    val starRating: Int? = null,
    val rating: Double? = null,
    val location: LocationDto? = null,
    val hotelFacilities: List<String>? = null,
    val hotelImages: List<HotelImageDto>? = null
)