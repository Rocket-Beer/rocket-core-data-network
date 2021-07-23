package com.rocket.android.core.data.network.model

import com.squareup.moshi.Json

internal class ApiResponse {
    open class ApiBase<Error>(
        @Json(name = "code") var code: String? = null,
        @Json(name = "message") var message: Error? = null
    ) : BaseNetworkApiResponse() {
        override fun isSuccess() = code == null
        override fun errorCode(): String? = code
        override fun errorData(): Error? = message

        override fun toString(): String {
            return "code=$code, message=$message"
        }
    }

    open class ApiBaseSimple() : ApiBase<String>() {
        override fun toString(): String {
            return "code=$code, message=$message"
        }
    }

    open class ApiBaseComplex() : ApiBase<ApiError>() {
        override fun toString(): String {
            return "code=$code, data=$message"
        }
    }

    data class ApiError(
        @Json(name = "id") var id: Int? = null,
        @Json(name = "description") var description: String? = null
    )

    data class SimpleListFake(
        @Json(name = "list") val list: List<SimpleFake>?
    ) : ApiBaseSimple() {
        override fun isSuccess() = list?.isNotEmpty() == true
    }

    data class SimpleFake(
        @Json(name = "id") val id: String?,
        @Json(name = "userId") val userId: String?,
        @Json(name = "title") val title: String?,
        @Json(name = "finished") val isFinished: Boolean?
    ) : ApiBaseSimple() {
        override fun isSuccess() = true
    }
}

internal class ApiRequest {
    data class SimpleFake(
        @Json(name = "id") val id: String,
        @Json(name = "userId") val userId: String,
        @Json(name = "title") val title: String,
        @Json(name = "finished") val isFinished: Boolean
    )
}

internal fun fakeApiRequestSimpleFake(): ApiRequest.SimpleFake = ApiRequest.SimpleFake(
    id = "",
    userId = "",
    title = "",
    isFinished = false
)