package com.rocket.android.core.data.network.interceptor

import com.rocket.core.data.network.commons.interceptor.BaseHeaderInterceptor

internal class HeaderInterceptor : BaseHeaderInterceptor() {
    override var headersMap: Map<String, String>
        get() = mutableMapOf(
            CONTENT_TYPE to APPLICATION_JSON,
            X_LANG to ES
        ).toMap()
        set(_) {}

    companion object HeaderConstants {
        const val CONTENT_TYPE = "Content-Type"
        const val APPLICATION_JSON = "application/json"
        const val X_LANG = "x-lang"
        const val ES = "ES"
    }
}
