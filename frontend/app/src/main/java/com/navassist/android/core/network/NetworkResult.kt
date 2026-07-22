package com.navassist.android.core.network

sealed interface NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>
    data class Success<out T>(val data: T) : NetworkResult<T>
    object Empty : NetworkResult<Nothing>
    data class Error(val exception: ApiException) : NetworkResult<Nothing>
    object Offline : NetworkResult<Nothing>
    object Unauthorized : NetworkResult<Nothing>
    object Timeout : NetworkResult<Nothing>
    object Cancelled : NetworkResult<Nothing>
}
