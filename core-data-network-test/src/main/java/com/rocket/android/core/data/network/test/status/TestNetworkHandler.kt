package com.rocket.android.core.data.network.test.status

import com.rocket.core.data.network.commons.status.NetworkHandler

class TestNetworkHandler : NetworkHandler {
    override val isConnected: Boolean get() = true
    override val isMetered: Boolean get() = false
}
