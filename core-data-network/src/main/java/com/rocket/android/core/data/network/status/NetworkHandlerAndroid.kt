package com.rocket.android.core.data.network.status

import android.content.Context
import android.net.ConnectivityManager
import com.rocket.core.data.network.commons.status.NetworkHandler

/**
 * Handler to manage device connectivity used on ConnectionInterceptors.
 * Uses deprecated code (pending to move to {@link NetworkStatusHandler}) but interceptor yet uses this handler
 */
class NetworkHandlerAndroid(private val context: Context) : NetworkHandler {
    override val isMetered: Boolean get() = context.connectivityManager.isActiveNetworkMetered

    override val isConnected: Boolean
        get() = context.connectivityManager.activeNetwork != null
}

val Context.connectivityManager: ConnectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
