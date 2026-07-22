package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.kyc.KycDocumentUploadResponseDto
import com.navassist.android.data.remote.dto.kyc.KycStatusResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface KycApi {
    @Multipart
    @POST("kyc/documents")
    suspend fun uploadDocuments(
        @Part("aadhaar_number") aadhaarNumber: RequestBody,
        @Part docFront: MultipartBody.Part,
        @Part docBack: MultipartBody.Part
    ): KycDocumentUploadResponseDto

    @GET("kyc/status")
    suspend fun getKycStatus(): KycStatusResponseDto
}
