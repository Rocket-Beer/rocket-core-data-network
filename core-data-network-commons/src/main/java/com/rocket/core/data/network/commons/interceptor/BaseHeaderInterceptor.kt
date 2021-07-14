package com.rocket.core.data.network.commons.interceptor

import okhttp3.Interceptor
import okhttp3.Response

@Suppress("unused")
abstract class BaseHeaderInterceptor() : Interceptor {
    abstract var headersMap: Map<String, String>

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val builder = originalRequest.newBuilder().apply {
            headersMap.forEach {
                val (key, value) = it
                header(key, value)
            }
        }
        val request = builder.method(originalRequest.method, originalRequest.body).build()
        return chain.proceed(request)
    }
}