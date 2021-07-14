package com.rocket.core.data.network.commons.logger

import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import retrofit2.Response
import java.io.IOException

private fun bodyToString(request: RequestBody?): String {
    return try {
        val buffer = Buffer()
        request ?: return ""
        request.writeTo(buffer)
        buffer.readUtf8()
    } catch (_: IOException) {
        ""
    }
}

fun mapFromRequest(request: Request): Map<String, String> = mapOf(
    NetworkLoggerHelper.LOG_REQUEST_METHOD to request.method,
    NetworkLoggerHelper.LOG_REQUEST_URL to "${request.url}",
    NetworkLoggerHelper.LOG_REQUEST_HEADERS to "${request.headers}"
)

fun mapFromResponse(response: Response<*>): MutableMap<String, String?> = mutableMapOf(
    NetworkLoggerHelper.LOG_RESPONSE_CODE to "${response.raw().code}",
    NetworkLoggerHelper.LOG_RESPONSE_URL to "${response.raw().request.url}",
    NetworkLoggerHelper.LOG_RESPONSE_MESSAGE to response.message(),
    NetworkLoggerHelper.LOG_RESPONSE_HEADERS to "${response.headers()}",
    NetworkLoggerHelper.LOG_RESPONSE_BODY to "${response.body()}",
    NetworkLoggerHelper.LOG_REQUEST_HEADERS to "${response.raw().request.headers}",
    NetworkLoggerHelper.LOG_REQUEST_BODY to bodyToString(response.raw().request.body)
)

object NetworkLoggerHelper {
    const val LOG_RESPONSE_CODE = "RESPONSE_CODE"
    const val LOG_RESPONSE_URL = "RESPONSE_URL"
    const val LOG_RESPONSE_MESSAGE = "RESPONSE_MESSAGE"
    const val LOG_RESPONSE_HEADERS = "RESPONSE_HEADERS"
    const val LOG_RESPONSE_BODY = "RESPONSE_BODY"

    const val LOG_REQUEST_METHOD = "REQUEST_METHOD"
    const val LOG_REQUEST_URL = "REQUEST_URL"
    const val LOG_REQUEST_BODY = "REQUEST_BODY"
    const val LOG_REQUEST_HEADERS = "REQUEST_HEADERS"
}
