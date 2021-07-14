package com.rocket.core.data.network.commons.interceptor

import com.rocket.core.crashreporting.logger.CrashLogger
import com.rocket.core.data.network.commons.error.NoConnectionException
import com.rocket.core.data.network.commons.logger.mapFromRequest
import com.rocket.core.data.network.commons.status.NetworkHandler
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Suppress("unused")
class ConnectionInterceptor(
    private val networkHandler: NetworkHandler,
    private val crashLogger: CrashLogger
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        when (networkHandler.isConnected) {
            true -> {
                return try {
                    chain.proceed(request)
                } catch (timeoutException: SocketTimeoutException) {
                    crashLogger.log(
                        exception = timeoutException,
                        map = mapFromRequest(request)
                    )
                    throw timeoutException
                } catch (unknownHostException: UnknownHostException) {
                    crashLogger.log(
                        exception = unknownHostException,
                        map = mapFromRequest(request)
                    )
                    throw unknownHostException
                } catch (exception: IOException) {
                    crashLogger.log(
                        exception = exception,
                        map = mapFromRequest(request)
                    )
                    throw exception
                }
            }
            false -> {
                val exception = NoConnectionException()
                crashLogger.log(
                    exception = exception,
                    map = mapFromRequest(request)
                )
                throw exception
            }
        }
    }
}
