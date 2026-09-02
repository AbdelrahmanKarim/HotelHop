package com.task.hotelhop.data.remote.service

import com.task.hotelhop.BuildConfig
import com.task.hotelhop.data.remote.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class HotelApiService(private val client: HttpClient) {

    suspend fun getHotels(limit: Int = 20, offset: Int = 0): HotelsListResponseDto {
        return client.get("${BuildConfig.BASE_URL}/hotels") {
            header("X-API-Key", BuildConfig.API_KEY)
            parameter("countryCode", "EG")
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
    }

    suspend fun getHotelDetails(hotelId: String): HotelDetailsWrapperDto {
        return client.get("${BuildConfig.BASE_URL}/hotel") {
            header("X-API-Key", BuildConfig.API_KEY)
            parameter("hotelId", hotelId)
        }.body()
    }

    suspend fun searchHotels(query: String, limit: Int = 10): SemanticSearchResponseDto {
        return client.get("${BuildConfig.BASE_URL}/hotels/semantic-search") {
            header("X-API-Key", BuildConfig.API_KEY)
            parameter("query", query)
            parameter("limit", limit)
        }.body()
    }
}