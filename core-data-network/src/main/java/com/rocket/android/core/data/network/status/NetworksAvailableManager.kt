package com.rocket.android.core.data.network.status

import android.content.Context
import android.net.Network
import android.net.NetworkCapabilities
import com.rocket.core.crashreporting.logger.CrashLogger
import com.rocket.core.crashreporting.logger.LogLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NetworksAvailableManager(
    context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val crashLogger: CrashLogger
) {
    private val connectivityManager = context.connectivityManager
    private var connectionsActive: MutableList<String> = mutableListOf()

    fun addConnection(network: Network, onAvailable: (NetworkStatus) -> Unit) {
        if (!hasAnyConnection()) {
            crashLogger.log(
                message = "NetworksAvailableManager restoring connection!",
                logLevel = LogLevel.DEBUG
            )
        }
        connectionsActive.add("$network")
        crashLogger.log(
            message = "NetworksAvailableManager added $network",
            logLevel = LogLevel.DEBUG
        )
        onAvailable(getNetworkInfo(network))
    }

    @ExperimentalCoroutinesApi
    fun loseConnection(network: Network, onLost: (NetworkStatus) -> Unit) {
        connectionsActive.remove("$network")
        crashLogger.log(
            message = "NetworksAvailableManager lost current $network",
            logLevel = LogLevel.DEBUG
        )

        CoroutineScope(dispatcher).launch {
            delay(NetworkStatusHandler.TIME_TO_DISCONNECT)
            if (!hasAnyConnection()) {
                crashLogger.log(
                    message = "NetworksAvailableManager no active connections!",
                    logLevel = LogLevel.DEBUG
                )
                onLost(NetworkStatus.NONE)
            }
        }
    }

    private fun hasAnyConnection() = connectionsActive.isNotEmpty()

    private fun getNetworkInfo(network: Network): NetworkStatus {
        return connectivityManager.getNetworkCapabilities(network)?.run {
            when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR
                else -> NetworkStatus.NONE
            }
        } ?: NetworkStatus.NONE
    }
}
