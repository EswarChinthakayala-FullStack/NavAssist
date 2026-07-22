package com.navassist.android.data.remote.dto.kyc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KycStatusResponseDto(
    @SerialName("verification_status") val verificationStatus: String = "NOT_SUBMITTED",
    @SerialName("message") val message: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("uploaded_at") val uploadedAt: String? = null,
    @SerialName("verified_at") val verifiedAt: String? = null
)

@Serializable
data class KycDocumentUploadResponseDto(
    @SerialName("verification_status") val verificationStatus: String = "PENDING",
    @SerialName("message") val message: String? = null
)
