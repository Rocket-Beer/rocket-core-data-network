package com.rocket.android.core.data.network.datasources

import com.rocket.android.core.data.network.datasource.BaseNetworkDatasource
import com.rocket.android.core.data.network.model.ApiRequest
import com.rocket.android.core.data.network.model.ApiResponse
import com.rocket.android.core.data.network.model.ApiResponse.SimpleListFake
import com.rocket.android.core.data.network.model.BaseNetworkApiResponse
import com.rocket.android.core.data.network.parser.MoshiJsonParser
import com.rocket.android.core.data.network.service.SimpleFakeApiService
import com.rocket.core.crashreporting.logger.CrashLogger
import com.rocket.core.domain.error.Failure
import com.rocket.core.domain.functional.Either

internal class SimpleNetworkDatasource(
    private val apiService: SimpleFakeApiService,
    crashLogger: CrashLogger
) : BaseNetworkDatasource(crashLogger) {

    suspend fun getAllSuspend(): Either<Failure, SimpleListFake?> {
        return requestSuspendApi(
            call = { apiService.getAllSuspend() },
            parserSuccess = { it }
        )
    }

    fun getAll(): Either<Failure, ApiResponse.SimpleListFake?> {
        return requestApi(
            call = { apiService.getAll() },
            parserSuccess = { it }
        )
    }

    fun getAllError(): Either<Failure, ApiResponse.SimpleListFake?> {
        return requestApi(
            call = { apiService.getAllError() },
            parserSuccess = { it },
            parserError = {
                Failure.GenericFailure(it?.errorBody())
            }
        )
    }

    suspend fun getAllSuspendGeneric(): Either<Failure, List<ApiResponse.SimpleFake>?> {
        return requestGenericSuspendApi(
            call = { apiService.getAllSuspendGeneric() },
            parserSuccess = { it }
        )
    }

    fun getAllGeneric(): Either<Failure, List<ApiResponse.SimpleFake>?> {
        return requestGenericApi(
            call = { apiService.getAllGeneric() },
            parserSuccess = { it }
        )
    }

    fun saveElement(data: ApiRequest.SimpleFake): Either<Failure, BaseNetworkApiResponse?> {
        return requestApi(
            call = { apiService.saveElement(data) },
            parserSuccess = { it }
        )
    }

    override fun parseErrorType(code: Int, message: Any?, body: String?): BaseNetworkApiResponse {
        return body?.let {
            if (it.isEmpty()) ApiResponse.ApiBase(
                code = code.toString(),
                message = message.toString()
            )
            else {
                try {
                    MoshiJsonParser().fromJson(it, ApiResponse.ApiBaseSimple::class.java)
                } catch (_: Exception) {
                    MoshiJsonParser().fromJson(it, ApiResponse.ApiBaseComplex::class.java)
                }
            }
        } ?: ApiResponse.ApiBase(
            code = code.toString(),
            message = message.toString()
        )
    }
}
