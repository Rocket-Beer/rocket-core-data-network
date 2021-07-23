package com.rocket.android.core.data.network.datasources

import arrow.core.Either
import com.rocket.android.core.data.commons.logging.CrashLogger
import com.rocket.android.core.data.network.datasource.BaseNetworkDatasource
import com.rocket.android.core.data.network.model.ApiRequest
import com.rocket.android.core.data.network.model.ApiResponse
import com.rocket.android.core.data.network.model.BaseNetworkApiResponse
import com.rocket.android.core.data.network.parser.MoshiJsonParser
import com.rocket.android.core.data.network.service.SimpleFakeApiService
import com.rocket.android.core.domain.error.Failure
import java.lang.Exception

internal class SimpleNetworkDatasource(
    private val apiService: SimpleFakeApiService,
    crashLogger: CrashLogger
) : BaseNetworkDatasource(crashLogger) {

    suspend fun getAllSuspend(): Either<Failure, ApiResponse.SimpleListFake?> {
        return requestApi(apiService.getAllSuspend()) { it }
    }

    fun getAll(): Either<Failure, ApiResponse.SimpleListFake?> {
        return requestApi(apiService.getAll()) { it }
    }

    suspend fun getAllSuspendGeneric(): Either<Failure, List<ApiResponse.SimpleFake>?> {
        return requestGenericApi(apiService.getAllSuspendGeneric()) { it }
    }

    fun getAllGeneric(): Either<Failure, List<ApiResponse.SimpleFake>?> {
        return requestGenericApi(apiService.getAllGeneric()) { it }
    }

    fun saveElement(data: ApiRequest.SimpleFake): Either<Failure, BaseNetworkApiResponse?> {
        return requestApi(apiService.saveElement(data)) { it }
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
                } catch(exception: Exception) {
                    MoshiJsonParser().fromJson(it, ApiResponse.ApiBaseComplex::class.java)
                }
            }
        } ?: ApiResponse.ApiBase(
            code = code.toString(),
            message = message.toString()
        )
    }
}