package com.rocket.android.core.data.network.di

import android.content.Context
import com.rocket.android.core.data.network.status.NetworkHandlerAndroid
import com.rocket.android.core.data.network.status.NetworkStatusHandler
import com.rocket.android.core.data.network.status.NetworksAvailableManager
import com.rocket.core.crashreporting.di.CoreCrashProvider
import com.rocket.core.crashreporting.logger.CrashLogger
import com.rocket.core.data.network.commons.di.createOkHttpClient
import com.rocket.core.data.network.commons.interceptor.BaseHeaderInterceptor
import com.rocket.core.data.network.commons.interceptor.ConnectionInterceptor
import com.rocket.core.domain.di.CoreProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Core dataNetwork provider for dependency injection, as a service locator, indicates default
 * implementations for all network necessary objects.
 *
 * Use it in your dependency injector library:
 * @code CoreDataNetworkProvider.getInstance(context)
 *
 * Create OkHttpClient, use data commons network extension
 * {@link com.rocket.android.core.data.commons.network.di.createOkHttpClient} to do it:
 *  createOkHttpClient(
 *      connectionInterceptor = coreDataNetworkProvider.connectionInterceptor,
 *      loggingInterceptor = coreDataNetworkProvider.httpLoggingInterceptor,
 *      headerInterceptor = BaseHeaderInterceptor
 *  )
 *
 * Create ApiService, use data commons network extension
 * {@link com.rocket.android.core.data.commons.network.di.createApiClient} to do it:
 *  createApiClient(
 *      okHttpClient = okHttpClient,
 *      baseUrl = url
 *  )
 *
 */
@Suppress("unused")
open class CoreDataNetworkProvider private constructor(private val context: Context) :
    CoreProvider() {

    object DataNetworkProperty {
        const val CONNECT_TIMEOUT = "CONNECT_TIMEOUT"
        const val READ_TIMEOUT = "READ_TIMEOUT"
    }

    @Suppress("unused")
    val crashLogger: CrashLogger by lazy { coreCrashProvider.crashLogger }

    @Suppress("unused")
    val networkHandler: NetworkHandlerAndroid by lazy { NetworkHandlerAndroid(context) }

    @ExperimentalCoroutinesApi
    @Suppress("unused")
    val networkStatusHandler: NetworkStatusHandler by lazy {
        NetworkStatusHandler(context, networksAvailableManager)
    }

    @Suppress("unused")
    val headerInterceptor: BaseHeaderInterceptor? by lazy { null }

    @Suppress("unused")
    val connectionInterceptor: ConnectionInterceptor by lazy {
        ConnectionInterceptor(
            networkHandler = networkHandler,
            crashLogger = crashLogger
        )
    }

    @Suppress("unused")
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

    @Suppress("unused")
    val certificatePinner: CertificatePinner? by lazy { null }

    val okHttpClient: OkHttpClient by lazy {
        createOkHttpClient(
            connectionInterceptor = connectionInterceptor,
            loggingInterceptor = httpLoggingInterceptor,
            headerInterceptor = headerInterceptor,
            certificateSslPinner = certificatePinner,
            connectTimeout = getPropertyOrNull(DataNetworkProperty.CONNECT_TIMEOUT) ?: CONNECT_TIMEOUT,
            readTimeout = getPropertyOrNull(DataNetworkProperty.READ_TIMEOUT) ?: READ_TIMEOUT
        )
    }

    private val coreCrashProvider: CoreCrashProvider by lazy { CoreCrashProvider.getInstance() }

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    @ExperimentalCoroutinesApi
    private val networksAvailableManager: NetworksAvailableManager by lazy {
        NetworksAvailableManager(
            context = context,
            dispatcher = ioDispatcher,
            crashLogger = crashLogger
        )
    }

    companion object {
        const val CONNECT_TIMEOUT = 60L
        const val READ_TIMEOUT = 60L

        @Suppress("ObjectPropertyName")
        private lateinit var _instance: CoreDataNetworkProvider

        fun getInstance(context: Context): CoreDataNetworkProvider =
            synchronized(this) {
                if (Companion::_instance.isInitialized) {
                    _instance
                } else {
                    _instance = CoreDataNetworkProvider(context)
                    _instance
                }
            }
    }
}
