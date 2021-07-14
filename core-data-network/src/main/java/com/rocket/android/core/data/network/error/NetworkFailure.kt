package com.rocket.android.core.data.network.error

import com.rocket.core.domain.error.Failure

/**
 * Sealed class for Failure on network core.
 * Each object/class is prepared for particular failures. For generic failures, use ServerFailure.
 * @constructor nullable message to create Failure
 */
sealed class NetworkFailure(data: Any? = null) : Failure.FeatureFailure(data) {

    class ServerFailure(val code: String? = "0", data: Any? = null) : NetworkFailure(data)
    object Timeout : NetworkFailure()
    object UnknownHost : NetworkFailure()
    class JsonFormat(data: String?) : NetworkFailure(data)
    object NoInternetConnection : NetworkFailure()
    object NotAuthorized : NetworkFailure()
    object Untrusted : NetworkFailure()
    class TooManyRequests(val code: Int) : NetworkFailure()
    object NetworkConnection : NetworkFailure()
}
