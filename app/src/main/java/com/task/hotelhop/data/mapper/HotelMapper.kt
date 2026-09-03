package com.task.hotelhop.data.mapper


import com.task.hotelhop.data.local.entity.HotelEntity
import com.task.hotelhop.data.remote.dto.*
import com.task.hotelhop.domain.entity.Hotel

fun HotelDto.toDomain(): Hotel {
    val mockPrice = (stars ?: 3) * 50.0 + 49.99

    return Hotel(
        id = id,
        name = name,
        city = city,
        rating = rating ?: 0.0,
        pricePerNight = mockPrice,
        mainImage = main_photo ?: "",
        description = hotelDescription ?: "",
        isFavorite = false
    )
}

fun HotelDetailsDto.toDomain(): Hotel {
    val mockPrice = (starRating ?: 3) * 50.0 + 49.99
    val lat = location?.latitude ?: 0.0
    val lng = location?.longitude ?: 0.0
    val mapUrl = if (lat != 0.0) "geo:$lat,$lng?q=$lat,$lng($name)" else ""

    return Hotel(
        id = id,
        name = name,
        city = city,
        rating = rating ?: 0.0,
        pricePerNight = mockPrice,
        mainImage = main_photo ?: "",
        images = hotelImages?.map { it.url } ?: emptyList(),
        amenities = hotelFacilities ?: emptyList(),
        description = hotelDescription ?: "",
        locationDetails = address ?: "",
        locationUrl = mapUrl,
        isFavorite = false
    )
}

fun SemanticHotelDto.toDomain(): Hotel {
    return Hotel(
        id = id,
        name = name,
        city = city,
        rating = (score ?: 0.0) * 10,
        pricePerNight = 149.99, // Fallback price
        mainImage = main_photo ?: "",
        locationDetails = address ?: "",
        isFavorite = false
    )
}

fun HotelDto.toEntity(): HotelEntity {
    val mockPrice = (stars ?: 3) * 50.0 + 49.99
    return HotelEntity(
        id = id, name = name, city = city, rating = rating ?: 0.0, pricePerNight = mockPrice,
        mainImage = main_photo ?: "", images = emptyList(), amenities = emptyList(),
        description = hotelDescription ?: "", locationDetails = "", locationUrl = "",
        isFavorite = false
    )
}

fun HotelEntity.toDomain(): Hotel {
    return Hotel(
        id = id, name = name, city = city, rating = rating, pricePerNight = pricePerNight,
        mainImage = mainImage, images = images, amenities = amenities,
        description = description, locationDetails = locationDetails,
        locationUrl = locationUrl, isFavorite = isFavorite
    )
}

fun Hotel.toEntity(): HotelEntity {
    return HotelEntity(
        id = id, name = name, city = city, rating = rating, pricePerNight = pricePerNight,
        mainImage = mainImage, images = images, amenities = amenities,
        description = description, locationDetails = locationDetails,
        locationUrl = locationUrl, isFavorite = isFavorite
    )
}