package com.rocket.android.core.data.network.model

/**
 * Base api response with auxiliary methods to check successful and obtain error data
 * @code Sample code:
 * open class BaseSimpleFakeApiResponse(
 *      @Json(name = "code") var code: String? = null,
 *      @Json(name = "message") var message: Any? = null
 *  ) : BaseNetworkApiResponse() {
 *      override fun isSuccess() = code == null
 *      override fun errorCode(): String? = code
 *      override fun errorMessage(): Any? = message
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
 *
 *  Success json response:
 *  {
 *      "list":[
 *          {
 *              "id":"1",
 *              "userId":"2",
 *              "title":"Finish this kata",
 *              "finished":false
 *          },
 *          {
 *              "id":"2",
 *              "userId":"2",
 *              "title":"Finish this kata",
 *              "finished":false
 *          }
 *      ]
 *  }
 *  Error json response:
 *  {
 *      "code":"ERR-001",
 *      "message":"Error message"
 *  }
 *
 *  Or with a complex error response:
 * @code Sample code:
 * open class BaseComplexFakeApiResponse(
 *      @Json(name = "code") var code: String? = null,
 *      @Json(name = "data") var data: Error? = null
 *  ) : BaseNetworkApiResponse() {
 *      override fun isSuccess() = code == null
 *      override fun errorCode(): String? = code
 *      override fun errorMessage(): Error? = message
 *  }
 *
 *  data class Error(
 *      @Json(name = "message") val message: String?,
 *      @Json(name = "stack") val stack: List<String>?
 *  ) : BaseComplexFakeApiResponse()
 *
 *  Error json response:
 *  {
 *      "code":"ERR-001",
 *      "data": {
 *          "message": "Error message",
 *          "stack": []
 *      }
 *  }
 */
open class BaseNetworkApiResponse {
    open fun isSuccess(): Boolean = true
    open fun errorCode(): String? = null
    open fun errorData(): Any? = null
}
