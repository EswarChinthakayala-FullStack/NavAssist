package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.message.ChatMessageDto
import com.navassist.android.data.remote.dto.message.SendMessageRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {
    @POST("messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequestDto): ChatMessageDto

    @GET("messages/{booking_id}")
    suspend fun getMessages(@Path("booking_id") bookingId: String): List<ChatMessageDto>
}
