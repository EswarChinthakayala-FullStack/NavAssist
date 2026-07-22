package com.navassist.android.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody

@Serializable
private data class ErrorResponseDto(
    @SerialName("detail") val detail: String? = null,
    @SerialName("message") val message: String? = null
)

object ErrorParser {
    fun parse(responseBody: ResponseBody?, statusCode: Int): ApiException {
        val errorText = responseBody?.string()
        val backendMessage = if (!errorText.isNull_or_empty()) {
            try {
                val nonNullText = errorText!!
                val parsed = JsonConfiguration.instance.decodeFromString<ErrorResponseDto>(nonNullText)
                parsed.detail ?: parsed.message ?: nonNullText
            } catch (e: Exception) {
                errorText
            }
        } else null

        val displayMsg = backendMessage ?: "An error occurred ($statusCode)"

        return when (statusCode) {
            400 -> ApiException.BadRequest(displayMsg, backendMessage)
            401 -> ApiException.Unauthorized(displayMsg)
            403 -> ApiException.Forbidden(displayMsg)
            404 -> ApiException.NotFound(displayMsg)
            409 -> ApiException.Conflict(displayMsg, backendMessage)
            422 -> ApiException.UnprocessableEntity(displayMsg, backendMessage)
            429 -> ApiException.TooManyRequests(displayMsg)
            in 500..599 -> ApiException.ServerError(displayMsg, statusCode)
            else -> ApiException.Unknown(displayMsg)
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
