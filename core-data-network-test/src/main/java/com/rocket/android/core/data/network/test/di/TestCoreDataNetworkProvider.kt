package com.rocket.android.core.data.network.test.di

import com.rocket.android.core.data.network.test.logger.TestLogger
import com.rocket.android.core.data.network.test.status.TestNetworkHandler
import com.rocket.core.crashreporting.logger.CrashLogger
import com.rocket.core.data.network.commons.di.createOkHttpClient
import com.rocket.core.data.network.commons.interceptor.ConnectionInterceptor
import com.rocket.core.data.network.commons.status.NetworkHandler
import com.rocket.core.domain.di.CoreProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@Suppress("unused")
class TestCoreDataNetworkProvider private constructor() : CoreProvider() {
    val crashLogger: CrashLogger by lazy { TestLogger() }

    val networkHandler: NetworkHandler by lazy { TestNetworkHandler() }

    val connectionInterceptor: ConnectionInterceptor by lazy {
        ConnectionInterceptor(
            networkHandler = networkHandler,
            crashLogger = crashLogger
        )
    }

    val httpLoggingInterceptor: HttpLoggingInterceptor by lazy {
        val debuggable = getPropertyOrNull(CoreProviderProperty.PRINT_LOGS) ?: false
        HttpLoggingInterceptor().apply {
            level = if (debuggable) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        val connectTimeout = getPropertyOrNull(DataNetworkProperty.CONNECT_TIMEOUT) ?: 60L
        val readTimeout = getPropertyOrNull(DataNetworkProperty.READ_TIMEOUT) ?: 60L

        createOkHttpClient(
            connectionInterceptor = connectionInterceptor,
            loggingInterceptor = httpLoggingInterceptor,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout
        )
    }

    companion object {
        @Suppress("ObjectPropertyName")
        private lateinit var _instance: TestCoreDataNetworkProvider

        fun getInstance(): TestCoreDataNetworkProvider =
            synchronized(this) {
                if (Companion::_instance.isInitialized) {
                    _instance
                } else {
                    _instance = TestCoreDataNetworkProvider()
                    _instance
                }
            }
    }

    object DataNetworkProperty {
        const val CONNECT_TIMEOUT = "CONNECT_TIMEOUT"
        const val READ_TIMEOUT = "READ_TIMEOUT"
        const val PRINT_LOGS = "PRINT_LOGS"
    }
}
