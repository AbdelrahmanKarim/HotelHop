package com.task.hotelhop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val latitude: Double,
    val longitude: Double
)