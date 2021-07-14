package com.rocket.android.core.data.network.status

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class NetworkStatus {
    WIFI,
    CELLULAR,
    NONE
}

/**
 * Handler to manage device connectivity. Uses `status` StateFlow to emit connection status.
 * Pending to implement ConnectionInterceptors to this handler.
 */
@ExperimentalCoroutinesApi
class NetworkStatusHandler constructor(
    context: Context,
    networksAvailableManager: NetworksAvailableManager
) {
    @ExperimentalCoroutinesApi
    private val _status = MutableStateFlow(value = NetworkStatus.NONE)

    /**
     * Hot flow to manage connection status {@link kotlinx.coroutines.flow.StateFlow}
     */
    @ExperimentalCoroutinesApi
    @Suppress("unused")
    val status: StateFlow<NetworkStatus>
        get() = _status

    private val connectivityManager = context.connectivityManager

    @ExperimentalCoroutinesApi
    private val networkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networksAvailableManager.addConnection(network) { _status.value = it }
            }

            override fun onLost(network: Network) {
                networksAvailableManager.loseConnection(network) { _status.value = it }
            }
        }

    init {
        startNetworkStatusTracking(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                .build()
        )
    }

    @Suppress("unused")
    fun startNetworkStatusTracking(request: NetworkRequest) {
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    @Suppress("unused")
    fun stopNetworkStatusTracking() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    companion object {
        const val TIME_TO_DISCONNECT = 5000L
    }
}
