package com.navassist.android.core.network

sealed class ApiException(
    override val message: String,
    val statusCode: Int = 0,
    val backendMessage: String? = null,
    val isRetryable: Boolean = false
) : Exception(message) {

    class BadRequest(msg: String, backendMsg: String? = null) :
        ApiException(message = msg, statusCode = 400, backendMessage = backendMsg, isRetryable = false)

    class Unauthorized(msg: String = "Session expired. Please log in again.") :
        ApiException(message = msg, statusCode = 401, isRetryable = false)

    class Forbidden(msg: String = "Access denied.") :
        ApiException(message = msg, statusCode = 403, isRetryable = false)

    class NotFound(msg: String = "Resource not found.") :
        ApiException(message = msg, statusCode = 404, isRetryable = false)

    class Conflict(msg: String, backendMsg: String? = null) :
        ApiException(message = msg, statusCode = 409, backendMessage = backendMsg, isRetryable = false)

    class UnprocessableEntity(msg: String, backendMsg: String? = null) :
        ApiException(message = msg, statusCode = 422, backendMessage = backendMsg, isRetryable = false)

    class TooManyRequests(msg: String = "Rate limit exceeded. Please wait.") :
        ApiException(message = msg, statusCode = 429, isRetryable = true)

    class ServerError(msg: String = "Internal server error. Please try again later.", code: Int = 500) :
        ApiException(message = msg, statusCode = code, isRetryable = true)

    class NetworkTimeout(msg: String = "Request timed out. Please check your connection.") :
        ApiException(message = msg, statusCode = 408, isRetryable = true)

    class NoInternet(msg: String = "No internet connection detected.") :
        ApiException(message = msg, statusCode = -1, isRetryable = true)

    class Unknown(msg: String, cause: Throwable? = null) :
        ApiException(message = msg, statusCode = -2, isRetryable = false)
}
