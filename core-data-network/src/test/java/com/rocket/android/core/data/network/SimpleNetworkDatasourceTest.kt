package com.rocket.android.core.data.network

import com.google.common.truth.Truth.assertThat
import com.rocket.android.core.data.network.datasources.SimpleNetworkDatasource
import com.rocket.android.core.data.network.error.NetworkFailure
import com.rocket.android.core.data.network.interceptor.HeaderInterceptor
import com.rocket.android.core.data.network.model.ApiResponse
import com.rocket.android.core.data.network.model.fakeApiRequestSimpleFake
import com.rocket.android.core.data.network.service.SimpleFakeApiService
import com.rocket.android.core.data.network.test.mockserver.MockWebServerTest
import com.rocket.android.core.data.network.test.utils.l
import com.rocket.android.core.data.network.test.utils.r
import com.rocket.core.data.network.commons.di.createApiClient
import com.rocket.core.data.network.commons.interceptor.BaseHeaderInterceptor
import com.rocket.core.domain.functional.Either
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class SimpleNetworkDatasourceTest : MockWebServerTest() {
    private lateinit var sut: SimpleNetworkDatasource

    private lateinit var headerInterceptor: BaseHeaderInterceptor
    private lateinit var apiService: SimpleFakeApiService

    @BeforeEach
    override fun init() {
        super.init()
        initDI()
    }

    override fun configureOkHttpClient(headerInterceptor: BaseHeaderInterceptor?) {
        super.configureOkHttpClient(headerInterceptor)
        configureDatasource()
    }

    override fun configureNetworkTimeout() {
        super.configureNetworkTimeout()
        configureDatasource()
    }

    override fun configureUnknownHost() {
        super.configureUnknownHost()
        configureDatasource()
    }

    override fun configureNetworkException() {
        super.configureNetworkException()
        configureDatasource()
    }

    private fun initDI() {
        headerInterceptor = HeaderInterceptor()
        configureOkHttpClient(headerInterceptor)
        configureDatasource()
    }

    private fun configureDatasource() {
        apiService = createApiClient(
            okHttpClient = okHttpClient,
            baseUrl = baseEndpoint
        )

        sut = SimpleNetworkDatasource(
            apiService = apiService,
            crashLogger = testCoreDataNetworkProvider.crashLogger
        )
    }

    @DisplayName("Test requests with base api responses")
    @Nested
    inner class ObjectResponses {
        @Test
        fun `server response object successfully`() {
            enqueueMockResponse(200, "json/getFakesObjectResponse.json")

            val either = sut.getAll()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Right::class.java)
            val result = either.r()
            assertThat(result).isInstanceOf(ApiResponse.SimpleListFake::class.java)
        }

        @Test
        fun `server response object error`() {
            enqueueMockResponse(200, "json/getFakesByIdResponse.json")

            val either = sut.getAllError()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
        }

        @Test
        fun `server response empty 204 successfully`() {
            enqueueMockResponse(204, "json/emptyFakeResponse.json")

            val either = sut.saveElement(fakeApiRequestSimpleFake())

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Right::class.java)
            val result = either.r()
            assertNull(result)
        }

        @Test
        fun `server response error with 200 with error data`() {
            enqueueMockResponse(200, "json/errorResponse.json")
            val expectedErrorCode = "ERR-001"
            val expectedErrorMessage = "Error message"

            val either = sut.getAll()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            with(result as? NetworkFailure.ServerFailure?) {
                assertNotNull(this?.code)
                assertEquals(expectedErrorCode, this?.code)
                assertNotNull(this?.data)
                assertEquals(expectedErrorMessage, this?.data)
            }
        }

        @Test
        fun `server response error 400`() {
            enqueueMockResponse(400, "json/emptyFakeResponse.json")
            val expectedErrorCode = "400"

            val either = sut.saveElement(fakeApiRequestSimpleFake())

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            with(result as? NetworkFailure.ServerFailure?) {
                assertNotNull(this?.code)
                assertEquals(expectedErrorCode, this?.code)
            }
        }

        @Test
        fun `server response error 400 with simple error data`() {
            enqueueMockResponse(400, "json/errorResponse.json")
            val expectedErrorCode = "ERR-001"
            val expectedErrorMessage = "Error message"

            val either = sut.getAll()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            with(result as? NetworkFailure.ServerFailure?) {
                assertNotNull(this?.code)
                assertEquals(expectedErrorCode, this?.code)
                assertEquals(expectedErrorMessage, this?.data)
            }
        }

        @Test
        fun `server response error 400 with complex error data`() {
            enqueueMockResponse(400, "json/errorComplexResponse.json")
            val expectedErrorCode = "ERR-001"
            val expectedErrorMessage = "Error message"

            val either = sut.getAll()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            with(result as? NetworkFailure.ServerFailure?) {
                assertNotNull(this?.code)
                assertEquals(expectedErrorCode, this?.code)
                with(this?.data as ApiResponse.ApiError) {
                    assertNotNull(this)
                    assertEquals(expectedErrorMessage, this.description)
                }
            }
        }
    }

    @DisplayName("Test requests with generic responses")
    @Nested
    inner class GenericResponses {
        @Test
        fun `server response generic error 500 (Internal Server Error)`() {
            enqueueMockResponse(500, "json/errorGenericResponse.json")

            val either = sut.getAllGeneric()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            with(result as? NetworkFailure.ServerFailure?) {
                assertNotNull(this?.code)
                assertEquals("500", this?.code)
                assertEquals("Server Error:Internal server error", this?.data)
            }
        }

        @Test
        fun `server response generic error 401 (Unauthorized)`() {
            enqueueMockResponse(401, "json/errorGenericResponse.json")

            val either = sut.getAllGeneric()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.NotAuthorized::class.java)
        }

        @Test
        fun `server response generic list successfully`() {
            enqueueMockResponse(200, "json/getFakesListResponse.json")

            val either = sut.getAllGeneric()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Right::class.java)
            val result = either.r()
            assertThat(result).isInstanceOf(List::class.java)
            result?.forEach {
                assertThat(it).isInstanceOf(ApiResponse.SimpleFake::class.java)
            }
        }
    }

    @DisplayName("Test suspendable requests calls")
    @Nested
    inner class SuspendResponses {
        @Test
        fun `suspend server response object successfully`() = runBlocking {
            enqueueMockResponse(200, "json/getFakesObjectResponse.json")

            val either = sut.getAllSuspend()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Right::class.java)
            val result = either.r()
            assertThat(result).isInstanceOf(ApiResponse.SimpleListFake::class.java)
        }

        @Test
        fun `suspend server response error 400`() = runBlocking {
            enqueueMockResponse(400, "json/emptyFakeResponse.json")
            val expectedErrorCode = "400"

            val either = sut.getAllSuspend()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            with(result as? NetworkFailure.ServerFailure?) {
                assertNotNull(this?.code)
                assertEquals(expectedErrorCode, this?.code)
            }
            Unit
        }

        @Test
        fun `suspend server response error 400 with error data`() = runBlocking {
            enqueueMockResponse(400, "json/errorComplexResponse.json")
            val expectedErrorCode = "ERR-001"
            val expectedErrorMessage = "Error message"

            val either = sut.getAllSuspend()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            with(result as? NetworkFailure.ServerFailure?) {
                assertNotNull(this?.code)
                assertEquals(expectedErrorCode, this?.code)
                with(this?.data as ApiResponse.ApiError) {
                    assertNotNull(this)
                    assertEquals(expectedErrorMessage, this.description)
                }
            }
            Unit
        }
    }

    @DisplayName("Test suspendable requests calls with generic responses")
    @Nested
    inner class SuspendGenericResponses {
        @Test
        fun `suspend server response generic list successfully`() = runBlocking {
            enqueueMockResponse(200, "json/getFakesListResponse.json")

            val either = sut.getAllSuspendGeneric()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Right::class.java)
            val result = either.r()
            assertThat(result).isInstanceOf(List::class.java)
            result?.forEach {
                assertThat(it).isInstanceOf(ApiResponse.SimpleFake::class.java)
            }
            Unit
        }

        @Test
        fun `suspend server response generic error 401 (Unauthorized)`() = runBlocking {
            enqueueMockResponse(401, "json/errorGenericResponse.json")

            val either = sut.getAllSuspendGeneric()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.NotAuthorized::class.java)
            Unit
        }

        @Test
        fun `suspend server response generic error 500 (Internal Server Error)`() = runBlocking {
            enqueueMockResponse(500, "json/errorGenericResponse.json")

            val either = sut.getAllSuspendGeneric()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            with(result as? NetworkFailure.ServerFailure?) {
                assertNotNull(this?.code)
                assertEquals("500", this?.code)
                assertEquals("Server Error:Internal server error", this?.data)
            }
            Unit
        }
    }

    @DisplayName("Check requests network exceptions")
    @Nested
    inner class NetworkExceptions {
        @Test
        fun `fails due to NoConnectionException if no connected`() {
            configureNetworkDisconnected()

            val either = sut.getAll()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.NoInternetConnection::class.java)
        }

        @Test
        fun `fails due to SocketTimeoutException if timeout`() {
            configureNetworkTimeout()

            val either = sut.getAll()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.Timeout::class.java)
        }

        @Test
        fun `fails due to UnknownHostException if unknown host`() {
            configureUnknownHost()

            val either = sut.getAll()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.UnknownHost::class.java)
        }

        @Test
        fun `fails due to Exception if other network exception`() {
            configureNetworkException()

            val httpExceptionCode = "-401"
            val either = sut.getAll()

            assertNotNull(either)
            assertThat(either).isInstanceOf(Either.Left::class.java)
            val result = either.l()
            assertThat(result).isInstanceOf(NetworkFailure.ServerFailure::class.java)
            assertEquals(httpExceptionCode, (result as NetworkFailure.ServerFailure).code)
        }
    }
}
