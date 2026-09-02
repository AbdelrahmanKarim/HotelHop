package com.task.hotelhop.data.remote.service

import com.task.hotelhop.BuildConfig
import com.task.hotelhop.data.remote.dto.paymob.PaymobIntentionRequest
import com.task.hotelhop.data.remote.dto.paymob.PaymobIntentionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PaymobApiService(private val client: HttpClient) {

    suspend fun createIntention(request: PaymobIntentionRequest): PaymobIntentionResponse {
        return client.post("${BuildConfig.PAYMOB_BASE_URL}/v1/intention/") {
            header("Authorization", "Token ${BuildConfig.PAYMOB_SECRET_KEY}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
