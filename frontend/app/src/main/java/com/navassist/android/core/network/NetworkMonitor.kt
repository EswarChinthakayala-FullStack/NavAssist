package com.navassist.android.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    LOSING,
    UNAVAILABLE
}

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _networkStatus = MutableStateFlow(NetworkStatus.UNAVAILABLE)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    init {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkStatus.value = NetworkStatus.CONNECTED
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                _networkStatus.value = NetworkStatus.LOSING
            }

            override fun onLost(network: Network) {
                _networkStatus.value = NetworkStatus.DISCONNECTED
            }

            override fun onUnavailable() {
                _networkStatus.value = NetworkStatus.UNAVAILABLE
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
        val isCurrentlyConnected = connectivityManager.activeNetwork != null
        _networkStatus.value = if (isCurrentlyConnected) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }
}
