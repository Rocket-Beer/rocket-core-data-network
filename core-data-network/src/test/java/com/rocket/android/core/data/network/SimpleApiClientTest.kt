package com.rocket.android.core.data.network

import com.rocket.android.core.data.network.interceptor.HeaderInterceptor
import com.rocket.android.core.data.network.model.fakeApiRequestSimpleFake
import com.rocket.android.core.data.network.service.SimpleFakeApiService
import com.rocket.android.core.data.network.test.mockserver.MockWebServerTest
import com.rocket.core.data.network.commons.di.createApiClient
import com.rocket.core.data.network.commons.error.NoConnectionException
import com.rocket.core.data.network.commons.interceptor.BaseHeaderInterceptor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertFailsWith

internal class SimpleApiClientTest : MockWebServerTest() {
    private lateinit var sut: SimpleFakeApiService

    private lateinit var headerInterceptor: BaseHeaderInterceptor

    @BeforeEach
    override fun init() {
        super.init()
        initDI()
    }

    override fun configureOkHttpClient(headerInterceptor: BaseHeaderInterceptor?) {
        super.configureOkHttpClient(headerInterceptor)
        configureApiClient()
    }

    override fun configureNetworkTimeout() {
        super.configureNetworkTimeout()
        configureApiClient()
    }

    override fun configureUnknownHost() {
        super.configureUnknownHost()
        configureApiClient()
    }

    override fun configureNetworkException() {
        super.configureNetworkException()
        configureApiClient()
    }

    private fun initDI() {
        headerInterceptor = HeaderInterceptor()
        super.configureOkHttpClient(headerInterceptor)
        configureApiClient()
    }

    private fun configureApiClient() {
        sut = createApiClient(
            okHttpClient = okHttpClient,
            baseUrl = baseEndpoint
        )
    }

    @DisplayName("Check requests headers")
    @Nested
    inner class HeadersResponses {
        @Test
        fun `request contains content-type header as json`() {
            enqueueMockResponse(fileName = "json/emptyObjectFakeResponse.json")
            sut.getAll().execute()
            assertRequestContainsHeader(
                HeaderInterceptor.CONTENT_TYPE,
                HeaderInterceptor.APPLICATION_JSON
            )
        }

        @Test
        fun `request contains x-lang header as ES`() {
            enqueueMockResponse(fileName = "json/emptyObjectFakeResponse.json")
            sut.getAll().execute()
            assertRequestContainsHeader(HeaderInterceptor.X_LANG, HeaderInterceptor.ES)
        }
    }

    @DisplayName("Check requests methods")
    @Nested
    inner class NetworkRequestMethod {
        @Test
        fun `request send GET method`() {
            enqueueMockResponse(fileName = "json/emptyObjectFakeResponse.json")
            sut.getAll().execute()
            assertGetRequestSentTo("/all")
        }

        @Test
        fun `request send POST method`() {
            enqueueMockResponse(fileName = "json/emptyObjectFakeResponse.json")
            val apiRequesSimpleFake = fakeApiRequestSimpleFake()
            sut.saveElement(apiRequesSimpleFake).execute()
            assertPostRequestSentTo("/all")
        }

        @Test
        fun `request send PUT method`() {
            enqueueMockResponse(fileName = "json/emptyObjectFakeResponse.json")
            val id = "ID"
            val apiRequesSimpleFake = fakeApiRequestSimpleFake()
            sut.updateById(id, apiRequesSimpleFake).execute()
            assertPutRequestSentTo("/element/$id")
        }

        @Test
        fun `request send DELETE method`() {
            enqueueMockResponse(fileName = "json/emptyObjectFakeResponse.json")
            sut.removeAll().execute()
            assertDeleteRequestSentTo("/all")
        }

        @Test
        fun `request send HTTP method`() {
            enqueueMockResponse(fileName = "json/emptyObjectFakeResponse.json")
            val apiRequesSimpleFake = fakeApiRequestSimpleFake()
            sut.removeByElement(apiRequesSimpleFake).execute()
            assertDeleteRequestSentTo("/all")
        }
    }

    @DisplayName("Check requests network exceptions")
    @Nested
    inner class NetworkExceptions {
        @Test
        fun `request fails with NoConnectionException if no connected`() {
            configureNetworkDisconnected()

            assertFailsWith(NoConnectionException::class) {
                sut.getAll().execute()
            }
        }

        @Test
        fun `request fails with SocketTimeoutException if timeout`() {
            configureNetworkTimeout()

            assertFailsWith(SocketTimeoutException::class) {
                sut.getAll().execute()
            }
        }

        @Test
        fun `request fails with UnknownHostException if unknown host`() {
            configureUnknownHost()

            assertFailsWith(UnknownHostException::class) {
                sut.getAll().execute()
            }
        }

        @Test
        fun `request fails with Exception if other network exception`() {
            configureNetworkException()

            assertFailsWith(Exception::class) {
                sut.getAll().execute()
            }
        }
    }
}
