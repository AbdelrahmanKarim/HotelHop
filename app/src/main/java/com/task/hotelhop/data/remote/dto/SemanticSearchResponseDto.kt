package com.task.hotelhop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SemanticSearchResponseDto(
    val data: List<SemanticHotelDto>
)