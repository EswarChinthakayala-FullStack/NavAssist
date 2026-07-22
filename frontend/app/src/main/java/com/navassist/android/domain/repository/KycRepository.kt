package com.navassist.android.domain.repository

import com.navassist.android.data.remote.dto.kyc.KycStatusResponseDto
import java.io.File

data class KycStatusDomain(
    val verificationStatus: String,
    val message: String? = null,
    val rejectionReason: String? = null,
    val verifiedAt: String? = null
)

interface KycRepository {
    suspend fun getKycStatus(): Result<KycStatusDomain>
    suspend fun uploadDocuments(aadhaarNumber: String, docFrontFile: File, docBackFile: File): Result<KycStatusDomain>
}
