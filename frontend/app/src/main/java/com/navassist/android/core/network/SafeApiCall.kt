package com.navassist.android.core.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T
): NetworkResult<T> {
    return withContext(dispatcher) {
        try {
            val response = apiCall()
            if (response == null || (response is List<*> && response.isEmpty())) {
                NetworkResult.Empty
            } else {
                NetworkResult.Success(response)
            }
        } catch (e: CancellationException) {
            NetworkResult.Cancelled
        } catch (e: SocketTimeoutException) {
            NetworkResult.Error(ApiException.NetworkTimeout())
        } catch (e: UnknownHostException) {
            NetworkResult.Error(ApiException.NoInternet())
        } catch (e: IOException) {
            NetworkResult.Error(ApiException.NoInternet(e.message ?: "Network I/O failure"))
        } catch (e: HttpException) {
            val apiException = ErrorParser.parse(e.response()?.errorBody(), e.code())
            if (e.code() == 401) {
                NetworkResult.Unauthorized
            } else {
                NetworkResult.Error(apiException)
            }
        } catch (e: Exception) {
            NetworkResult.Error(ApiException.Unknown(e.message ?: "Unexpected error", e))
        }
    }
}
