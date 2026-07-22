package com.navassist.android.data.repository

import com.navassist.android.data.remote.api.KycApi
import com.navassist.android.domain.repository.KycRepository
import com.navassist.android.domain.repository.KycStatusDomain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KycRepositoryImpl @Inject constructor(
    private val kycApi: KycApi
) : KycRepository {

    override suspend fun getKycStatus(): Result<KycStatusDomain> {
        return try {
            val dto = kycApi.getKycStatus()
            Result.success(
                KycStatusDomain(
                    verificationStatus = dto.verificationStatus,
                    message = dto.message,
                    rejectionReason = dto.rejectionReason,
                    verifiedAt = dto.verifiedAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadDocuments(
        aadhaarNumber: String,
        docFrontFile: File,
        docBackFile: File
    ): Result<KycStatusDomain> {
        return try {
            val aadhaarBody = aadhaarNumber.toRequestBody("text/plain".toMediaTypeOrNull())

            val frontReq = docFrontFile.asRequestBody("image/*".toMediaTypeOrNull())
            val frontPart = MultipartBody.Part.createFormData("doc_front", docFrontFile.name, frontReq)

            val backReq = docBackFile.asRequestBody("image/*".toMediaTypeOrNull())
            val backPart = MultipartBody.Part.createFormData("doc_back", docBackFile.name, backReq)

            val response = kycApi.uploadDocuments(aadhaarBody, frontPart, backPart)
            Result.success(
                KycStatusDomain(
                    verificationStatus = response.verificationStatus,
                    message = response.message
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
