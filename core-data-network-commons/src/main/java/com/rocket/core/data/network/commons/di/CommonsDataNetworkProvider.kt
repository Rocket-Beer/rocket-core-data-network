package com.rocket.core.data.network.commons.di

import com.rocket.core.data.network.commons.interceptor.BaseHeaderInterceptor
import com.rocket.core.data.network.commons.interceptor.ConnectionInterceptor
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

fun createOkHttpClient(
    connectionInterceptor: ConnectionInterceptor,
    loggingInterceptor: HttpLoggingInterceptor,
    headerInterceptor: BaseHeaderInterceptor? = null,
    certificateSslPinner: CertificatePinner? = null,
    connectTimeout: Long = 60L,
    readTimeout: Long = 60L
): OkHttpClient {
    return OkHttpClient.Builder().apply {
        headerInterceptor?.let { addInterceptor(it) }
        addInterceptor(connectionInterceptor)
        addInterceptor(loggingInterceptor)
        certificateSslPinner?.let { certificatePinner(it) }
        connectTimeout(connectTimeout, TimeUnit.SECONDS)
        readTimeout(readTimeout, TimeUnit.SECONDS)
    }.build()
}

fun createConverterFactory(vararg jsonAdapters: JsonAdapter<Any>): Converter.Factory {
    return MoshiConverterFactory.create(
        Moshi.Builder().run {
            jsonAdapters.forEach { add(it) }
            add(KotlinJsonAdapterFactory())
            build()
        }
    )
}

inline fun <reified T> createApiClient(
    okHttpClient: OkHttpClient,
    baseUrl: String,
    converterFactory: Converter.Factory = createConverterFactory()
): T {
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .build()
    return retrofit.create(T::class.java)
}