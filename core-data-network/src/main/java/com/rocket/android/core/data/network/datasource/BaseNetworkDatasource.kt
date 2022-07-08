package com.rocket.android.core.data.network.datasource

import com.rocket.android.core.data.network.error.NetworkFailure
import com.rocket.android.core.data.network.error.NetworkFailure.ServerFailure
import com.rocket.android.core.data.network.model.BaseNetworkApiResponse
import com.rocket.core.crashreporting.logger.CrashLogger
import com.rocket.core.data.network.commons.error.NetworkException
import com.rocket.core.data.network.commons.error.NoConnectionException
import com.rocket.core.data.network.commons.logger.NetworkLoggerHelper.LOG_RESPONSE_BODY
import com.rocket.core.data.network.commons.logger.NetworkLoggerHelper.LOG_RESPONSE_CODE
import com.rocket.core.data.network.commons.logger.NetworkLoggerHelper.LOG_RESPONSE_MESSAGE
import com.rocket.core.data.network.commons.logger.mapFromResponse
import com.rocket.core.domain.error.Failure
import com.rocket.core.domain.functional.Either
import com.rocket.core.domain.functional.Either.Left
import com.rocket.core.domain.functional.Either.Right
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Base network datasource to simple manage network request, connection status, and reponse parsing.
 * Allow base responses (json object responses) or generic (json list responses).
 * @constructor crashLogger to register all request or parsing errors {@link CrashLogger}.
 * By default uses DefaultLogger which writes on console log.
 *
 * @code Sample code:
 * class SimpleNetworkDatasource(
 *      private val apiService: SimpleFakeApiService,
 *      crashLogger: CrashLogger
 * ) : BaseNetworkDatasource(crashLogger) {
 *
 *      //BaseApiResponse suspend request
 *      suspend fun getAllSuspend(): Either<Failure, ApiResponse.SimpleListFake?> {
 *          return requestApi(apiService.getAllSuspend()) { it }
 *      }
 *
 *      //BaseApiResponse request
 *      fun getAll(): Either<Failure, ApiResponse.SimpleListFake?> {
 *          return requestApi(apiService.getAll()) { it }
 *      }
 *
 *      //Generic suspend request
 *      suspend fun getAllSuspendGeneric(): Either<Failure, List<ApiResponse.SimpleFake>?> {
 *          return requestGenericApi(apiService.getAllSuspendGeneric()) { it }
 *      }
 *
 *      //Generic request
 *      fun getAllGeneric(): Either<Failure, List<ApiResponse.SimpleFake>?> {
 *          return requestGenericApi(apiService.getAllGeneric()) { it }
 *      }
 *
 *      fun saveElement(data: ApiRequest.SimpleFake)
 *      : Either<Failure, ApiResponse.BaseSimpleFakeApiResponse?> {
 *          return requestApi(apiService.saveElement(data)) { it }
 *      }
 *
 *      override fun parseErrorType(code: Int, message: String, body: String?)
 *      : BaseNetworkApiResponse {
 *          return body?.let {
 *              if (it.isEmpty())
 *                  ApiResponse.BaseSimpleFakeApiResponse(
 *                      code = code.toString(),
 *                      message = message
 *                  )
 *              else {
 *                  MoshiJsonParser()
 *                      .fromJson(it, ApiResponse.BaseSimpleFakeApiResponse::class.java)
 *              }
 *              } ?: run {
 *                  ApiResponse.BaseSimpleFakeApiResponse(code = code.toString(), message = message)
 *              }
 *          }
 *      }
 *
 *  //ApiService interface for requests
 *  interface SimpleFakeApiService {
 *      @GET(value = "/all")
 *      suspend fun getAllSuspend(): Response<ApiResponse.SimpleListFake>
 *
 *      @GET(value = "/all")
 *      fun getAll(): Call<ApiResponse.SimpleListFake>
 *
 *      @GET(value = "/all")
 *      suspend fun getAllSuspendGeneric(): Response<List<ApiResponse.SimpleFake>>
 *
 *      @GET(value = "/all")
 *      fun getAllGeneric(): Call<List<ApiResponse.SimpleFake>>
 * }
 *
 * //BaseApiResponse class with success and error data methods
 *  open class BaseSimpleFakeApiResponse(
 *      @Json(name = "code") var code: String? = null,
 *      @Json(name = "message") var message: String? = null
 *  ) : BaseNetworkApiResponse() {
 *      override fun isSuccess() = code == null
 *      override fun errorCode(): String? = code
 *      override fun errorMessage(): String? = message
 *  }
 *
 *  data class SimpleListFake(
 *      @Json(name = "list") val list: List<SimpleFake>?
 *  ) : BaseSimpleFakeApiResponse()
 *
 *  data class SimpleFake(
 *      @Json(name = "id") val id: String?,
 *      @Json(name = "userId") val userId: String?,
 *      @Json(name = "title") val title: String?,
 *      @Json(name = "finished") val isFinished: Boolean?
 *  ) : BaseSimpleFakeApiResponse()
 */
@Suppress("TooManyFunctions")
open class BaseNetworkDatasource(private val crashLogger: CrashLogger, private val logPath: File? = null) {
    //region Generic
    /** Suspendable execution of an generic api call (non BaseNetworkApiResponse response expected
     * {@link BaseNetworkApiResponse}) and parse success result.
     * @param call retrofit Response to execute.
     * @param parserSuccess lambda to parse success response.
     * @return success/unsuccess result wrapped into Either class.
     */
    protected open suspend fun <Api, Domain> requestGenericSuspendApi(
        call: suspend () -> Response<Api>,
        parserSuccess: (Api?) -> Domain
    ): Either<Failure, Domain> {
        return try {
            parseGenericResponse(call(), parserSuccess)
        } catch (error: IOException) {
            manageGenericRequestException(error)
        }
    }

    /** Executes an generic api call (non BaseNetworkApiResponse response expected
     * {@link BaseNetworkApiResponse}) and parse success result.
     * @param call retrofit Call to execute.
     * @param parserSuccess lambda to parse success response.
     * @return success/unsuccess result wrapped into Either class.
     */
    protected open fun <Api, Domain> requestGenericApi(
        call: () -> Call<Api>,
        parserSuccess: (Api?) -> Domain
    ): Either<Failure, Domain> {
        return try {
            parseGenericResponse(call().execute(), parserSuccess)
        } catch (error: IOException) {
            manageGenericRequestException(error)
        }
    }

    /** Parse response from generic api request and check http code to parse success or error.
     * @param response retrofit Response to parse.
     * @param parserSuccess lambda to parse success response.
     * @return success/unsuccess result wrapped into Either class.
     */
    private fun <Api, Domain> parseGenericResponse(
        response: Response<Api>,
        parserSuccess: (Api?) -> Domain
    ): Either<Failure, Domain> {
        return if (response.isSuccessful) {
            Right(parserSuccess(response.body()))
        } else {
            parseGenericError(response)
        }
    }

    /** Parse unsuccessful response body to apiError wrapped in Either object, and logs error.
     * @param response retrofit Response to parse.
     * @return {@link BaseNetworkApiResponse} either with apiError.
     */
    private fun <Error, Domain> parseGenericError(response: Response<Error>): Either<Failure, Domain> {
        crashLogger.log(
            exception = NetworkException("parseGenericError"),
            map = mapFromResponse(response),
            logPath = logPath
        )
        return Left(
            parseGenericErrorType(
                response.code(),
                response.message(),
                response.errorBody()?.string()
            )
        )
    }

    /** Parse throwable error to apiError wrapped in Either object, and logs error.
     * @param throwable error.
     * @return {@link BaseNetworkApiResponse} either with apiError.
     */
    private fun <Domain> manageGenericRequestException(error: Throwable): Either<Failure, Domain> {
        crashLogger.log(
            exception = NetworkException("manageGenericRequestException"),
            map = mapOf(LOG_RESPONSE_MESSAGE to error.message),
            logPath = logPath
        )
        return Left(
            when (error) {
                is NoConnectionException -> NetworkFailure.NoInternetConnection
                is SocketTimeoutException -> NetworkFailure.Timeout
                is UnknownHostException -> NetworkFailure.UnknownHost
                else -> NetworkFailure.ServerFailure(HTTP_EXCEPTION_CODE, error.message)
            }
        )
    }
    //endregion

    //region Base
    /** Suspendable execution of an api call (BaseNetworkApiResponse response expected
     * {@link BaseNetworkApiResponse}) and parse success result.
     * @param call retrofit Response to execute.
     * @param parserSuccess lambda to parse success response.
     * @return success/unsuccess result wrapped into Either class.
     */
    protected open suspend fun <Api : BaseNetworkApiResponse, Domain> requestSuspendApi(
        call: suspend () -> Response<Api>,
        parserSuccess: (Api?) -> Domain
    ): Either<Failure, Domain> {
        return try {
            parseResponse(call(), parserSuccess).mapLeft(::parseToFailure)
        } catch (error: IOException) {
            manageRequestException<Domain>(error).mapLeft(::parseToFailure)
        }
    }

    /** Executes an api call (BaseNetworkApiResponse response expected
     * {@link BaseNetworkApiResponse}) and parse success result.
     * @param call retrofit Call to execute.
     * @param parserSuccess lambda to parse success response.
     * @return success/unsuccess result wrapped into Either class.
     */
    protected open fun <Api : BaseNetworkApiResponse, Domain> requestApi(
        call: () -> Call<Api>,
        parserSuccess: (Api?) -> Domain,
        parserError: ((BaseNetworkApiResponse?) -> Failure)? = null
    ): Either<Failure, Domain> {
        return try {
            parseResponse(call().execute(), parserSuccess).mapLeft(parserError ?: (::parseToFailure))
        } catch (error: IOException) {
            manageRequestException<Domain>(error).mapLeft(::parseToFailure)
        }
    }

    /** Parse response from api request and check http code to parse success or error.
     * @param response retrofit Response to parse.
     * @param parserSuccess lambda to parse success response.
     * @return success/unsuccess result wrapped into Either class.
     */
    private fun <Api : BaseNetworkApiResponse?, Domain> parseResponse(
        response: Response<Api>,
        parserSuccess: (Api?) -> Domain
    ): Either<BaseNetworkApiResponse, Domain> {
        return if (response.isSuccessful) {
            parseApiResponse(response, parserSuccess)
        } else {
            parseErrorBody(response)
        }
    }

    /** Parse success response from api request, and check body response to parse success or error.
     * @param response retrofit Response to parse.
     * @param parserSuccess lambda to parse success response.
     * @return success/unsuccess result wrapped into Either class.
     */
    private fun <Api : BaseNetworkApiResponse?, Domain> parseApiResponse(
        response: Response<Api>,
        parserSuccess: (Api?) -> Domain
    ): Either<BaseNetworkApiResponse, Domain> {
        val data = response.body()
        return if (isSuccessful(response, data)) {
            Right(parserSuccess(data))
        } else {
            parseError(response)
        }
    }

    /** Checks if api call was successful. Uses `isSuccessful`method from BaseNetworkApiResponse.
     * @param response retrofit Response to check.
     * @param data response body.
     * @return boolean if api call was successful.
     */
    private fun <Api : BaseNetworkApiResponse?> isSuccessful(
        response: Response<Api>,
        data: Api?
    ): Boolean =
        response.isSuccessful && (data == null || data.isSuccess())

    /** Parse unsuccessful response body to apiError wrapped in Either object, and logs error.
     * @param response retrofit Response to parse.
     * @return {@link BaseNetworkApiResponse} either with apiError.
     */
    private fun <Error : BaseNetworkApiResponse?, Domain> parseError(response: Response<Error>):
        Either<BaseNetworkApiResponse, Domain> {
        crashLogger.log(
            exception = NetworkException("parseError"),
            map = mapFromResponse(response)
        )

        return when (val error = response.body()) {
            is BaseNetworkApiResponse -> Left(
                throwError(
                    code = error.errorCode() ?: UNKNOWN_ERROR_CODE,
                    message = error.errorData()
                )
            )
            else -> {
                parseErrorBody(response)
            }
        }
    }

    /** Parse unsuccessful response errorBody to apiError wrapped in Either object, and logs error.
     * @param response retrofit Response to parse.
     * @return {@link BaseNetworkApiResponse} either with apiError.
     */
    private fun <Domain> parseErrorBody(response: Response<*>):
        Either<BaseNetworkApiResponse, Domain> {
        val errorBody = response.errorBody()?.string()

        crashLogger.log(
            exception = NetworkException("parseErrorBody"),
            map = mapFromResponse(response)
                .also { map ->
                    map[LOG_RESPONSE_BODY] = errorBody
                }
        )

        return Left(
            parseErrorType(
                code = response.code(),
                message = response.message(),
                body = errorBody
            )
        )
    }

    /** Parse throwable error to apiError wrapped in Either object.
     * @param throwable error.
     * @return {@link BaseNetworkApiResponse} either with apiError.
     */
    private fun <Domain> manageRequestException(error: Throwable):
        Either<BaseNetworkApiResponse, Domain> {
        crashLogger.log(
            exception = NetworkException("manageRequestException"),
            map = mapOf(LOG_RESPONSE_MESSAGE to error.message)
        )

        return when (error) {
            is NoConnectionException -> Left(throwError(code = NO_CONNECTION))
            is SocketTimeoutException -> Left(throwError(code = TIMEOUT))
            is UnknownHostException -> Left(throwError(code = UNKNOWN_HOST))
            else -> Left(throwError(code = HTTP_EXCEPTION_CODE, message = error.message))
        }
    }

    /** Parse NetworkApiError object to NetworkFailure class.
     * @param {@link BaseNetworkApiResponse} apiError to convert.
     * @return {@link NetworkFailure} output failure por network apiError.
     */
    private fun parseToFailure(networkApiError: BaseNetworkApiResponse): NetworkFailure {
        crashLogger.log(
            exception = NetworkException("manageGenericRequestException"),
            map = mapOf(
                LOG_RESPONSE_CODE to networkApiError.errorCode(),
                LOG_RESPONSE_MESSAGE to networkApiError.errorData().toString()
            )
        )

        return when (networkApiError.errorCode()) {
            NO_CONNECTION -> NetworkFailure.NoInternetConnection
            TIMEOUT -> NetworkFailure.Timeout
            UNKNOWN_HOST -> NetworkFailure.UnknownHost
            JSON_FORMAT -> NetworkFailure.JsonFormat(networkApiError.errorData().toString())
            else -> NetworkFailure.ServerFailure(
                networkApiError.errorCode(),
                networkApiError.errorData()
            )
        }
    }

    /** Creates new apiResponse object with code and message error.
     * @param code error code.
     * @param message error message.
     * @return {@link BaseNetworkApiResponse} new apiError object.
     */
    private fun throwError(code: String?, message: Any? = null): BaseNetworkApiResponse =
        object :
            BaseNetworkApiResponse() {
            override fun isSuccess(): Boolean = false
            override fun errorCode(): String? = code
            override fun errorData(): Any? = message
        }

    /** Creates new apiResponse (BaseNetworkApiResponse response expected
     * {@link BaseNetworkApiResponse}) object with code and message error.
     * This could be overridden by new classes to parse body String into particular ApiError class.
     * Only needed for BaseNetworkApiResponse, not for generic requests.
     * @code Sample code:
     * override fun parseErrorType(code: Int, message: String, body: String?)
     * : BaseNetworkApiResponse {
     *      return body?.let {
     *          if (it.isEmpty())
     *              ApiResponse.BaseSimpleFakeApiResponse(
     *                  code = code.toString(),
     *                  message = message
     *              )
     *          else {
     *              MoshiJsonParser()
     *                  .fromJson(it, ApiResponse.BaseSimpleFakeApiResponse::class.java)
     *          }
     *      } ?: ApiResponse.BaseSimpleFakeApiResponse(code = code.toString(), message = message)
     * }
     * @param code error code.
     * @param message error message.
     * @param body response errorBody.
     * @return {@link BaseNetworkApiResponse} new apiError object.
     */
    open fun parseErrorType(code: Int, message: Any?, body: String?): BaseNetworkApiResponse =
        object :
            BaseNetworkApiResponse() {
            override fun isSuccess(): Boolean = false
            override fun errorCode(): String = code.toString()
            override fun errorData(): Any? = message
            override fun errorBody(): String? = body
        }

    /**
     * Return a network failure {@link NetworkFailure} with code and message error.
     * This could be overridden by new classes to handle any specific network error.
     * @param code error code.
     * @param message error message.
     * @param body response errorBody.
     * @return {@link NetworkFailure} network failure object.
     */
    open fun parseGenericErrorType(code: Int, message: String?, body: String?): NetworkFailure {
        return ServerFailure("$code", "$message:$body")
    }
    //endregion

    companion object {
        private const val UNKNOWN_ERROR_CODE = "-1"
        private const val NO_CONNECTION = "-400"
        private const val HTTP_EXCEPTION_CODE = "-401"
        private const val TIMEOUT = "-402"
        private const val UNKNOWN_HOST = "-403"
        private const val JSON_FORMAT = "-404"
    }
}
