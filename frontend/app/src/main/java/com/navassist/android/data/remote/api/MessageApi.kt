package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.message.ChatMessageDto
import com.navassist.android.data.remote.dto.message.SendMessageRequestDto
import com.navassist.android.data.remote.dto.message.UploadAttachmentResponseDto
import okhttp3.MultipartBody
import retrofit2.http.*

interface MessageApi {
    @GET("bookings/{bookingId}/messages")
    suspend fun getMessages(@Path("bookingId") bookingId: Int): List<ChatMessageDto>

    @POST("bookings/{bookingId}/messages")
    suspend fun sendMessage(
        @Path("bookingId") bookingId: Int,
        @Body request: SendMessageRequestDto
    ): ChatMessageDto

    @Multipart
    @POST("bookings/upload")
    suspend fun uploadAttachment(@Part file: MultipartBody.Part): UploadAttachmentResponseDto
}
