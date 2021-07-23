package com.rocket.android.core.data.network.interceptor

import com.rocket.android.core.data.commons.network.interceptor.BaseHeaderInterceptor

internal class HeaderInterceptor() : BaseHeaderInterceptor() {
    override var headersMap: Map<String, String>
        get() = mutableMapOf(
            CONTENT_TYPE to APPLICATION_JSON,
            X_LANG to ES
        ).toMap()
        set(_) {}

    companion object HeaderConstants {
        val CONTENT_TYPE = "Content-Type"
        val APPLICATION_JSON = "application/json"
        val X_LANG = "x-lang"
        val ES = "ES"
    }
}