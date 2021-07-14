package com.rocket.core.data.network.commons.error

import java.io.IOException

class NoConnectionException(msg: String = "Network not connected!") : IOException(msg)
class NetworkException(msg: String = "") : Exception(msg)