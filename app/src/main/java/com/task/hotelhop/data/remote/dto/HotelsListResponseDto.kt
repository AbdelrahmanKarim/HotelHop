package com.task.hotelhop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HotelsListResponseDto(
    val data: List<HotelDto>
)