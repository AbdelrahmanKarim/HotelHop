package com.task.hotelhop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HotelDetailsWrapperDto(
    val data: HotelDetailsDto
)