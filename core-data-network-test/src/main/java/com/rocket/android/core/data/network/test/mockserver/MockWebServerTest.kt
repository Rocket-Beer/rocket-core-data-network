package com.rocket.android.core.data.network.test.mockserver

import androidx.test.platform.app.InstrumentationRegistry
import com.rocket.android.core.data.network.test.di.TestCoreDataNetworkProvider
import com.rocket.core.data.network.commons.di.createOkHttpClient
import com.rocket.core.data.network.commons.interceptor.BaseHeaderInterceptor
import com.rocket.core.data.network.commons.interceptor.ConnectionInterceptor
import com.rocket.core.data.network.commons.status.NetworkHandler
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Suppress("unused")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class MockWebServerTest {

    companion object {
        private const val FILE_ENCODING = "UTF-8"
    }

    private var server: MockWebServer = MockWebServer()

    val testCoreDataNetworkProvider: TestCoreDataNetworkProvider =
        TestCoreDataNetworkProvider.getInstance()
    private val networkHandler: NetworkHandler = mockk()
    private var connectionInterceptor: ConnectionInterceptor

    lateinit var okHttpClient: OkHttpClient

    protected val baseEndpoint: String
        get() = server.url("/").toString()

    init {
        connectionInterceptor = ConnectionInterceptor(
            networkHandler = networkHandler,
            crashLogger = testCoreDataNetworkProvider.crashLogger
        )
    }

    @BeforeAll
    fun setUp() {
        server.start()
    }

    @BeforeEach
    open fun init() {
        configureNetworkConnected()
    }

    @AfterAll
    fun tearDown() {
        server.shutdown()
    }

    @JvmOverloads
    open fun configureOkHttpClient(headerInterceptor: BaseHeaderInterceptor? = null) {
        okHttpClient = createOkHttpClient(
            connectionInterceptor = connectionInterceptor,
            loggingInterceptor = testCoreDataNetworkProvider.httpLoggingInterceptor,
            headerInterceptor = headerInterceptor
        )
    }

    //region Configure Network status
    open fun configureNetworkConnected() {
        every { networkHandler.isConnected } returns true
        okHttpClient = createOkHttpClient(
            connectionInterceptor = connectionInterceptor,
            loggingInterceptor = testCoreDataNetworkProvider.httpLoggingInterceptor
        )
    }

    open fun configureNetworkDisconnected() {
        every { networkHandler.isConnected } returns false
        okHttpClient = createOkHttpClient(
            connectionInterceptor = connectionInterceptor,
            loggingInterceptor = testCoreDataNetworkProvider.httpLoggingInterceptor
        )
    }

    open fun configureNetworkTimeout() {
        every { networkHandler.isConnected } returns true
        okHttpClient = createOkHttpClient(
            connectionInterceptor = mockk {
                every { intercept(any()) } throws SocketTimeoutException()
            },
            loggingInterceptor = testCoreDataNetworkProvider.httpLoggingInterceptor
        )
    }

    open fun configureUnknownHost() {
        every { networkHandler.isConnected } returns true
        okHttpClient = createOkHttpClient(
            connectionInterceptor = mockk {
                every { intercept(any()) } throws UnknownHostException()
            },
            loggingInterceptor = testCoreDataNetworkProvider.httpLoggingInterceptor
        )
    }

    open fun configureNetworkException() {
        every { networkHandler.isConnected } returns true
        okHttpClient = createOkHttpClient(
            connectionInterceptor = mockk {
                every { intercept(any()) } throws IOException()
            },
            loggingInterceptor = testCoreDataNetworkProvider.httpLoggingInterceptor
        )
    }
    //endregion

    fun enqueueMockResponse(
        code: Int = 200,
        fileName: String? = null
    ) {
        val mockResponse = MockResponse()
        val fileContent = getContentFromKotlinFile(fileName)
        mockResponse.setResponseCode(code)
        mockResponse.setBody(fileContent)
        server.enqueue(mockResponse)
    }

    fun enqueueAndroidMockResponse(
        code: Int = 200,
        fileName: String? = null
    ) {
        val mockResponse = MockResponse()
        val fileContent = getContentFromAndroidFile(fileName)
        mockResponse.setResponseCode(code)
        mockResponse.setBody(fileContent)
        server.enqueue(mockResponse)
    }

    //region aux assertions
    protected fun assertRequestSentTo(url: String) {
        val request = server.takeRequest()
        assertEquals(url, request.path)
    }

    protected fun assertGetRequestSentTo(url: String) {
        val request = server.takeRequest()
        assertEquals(url, request.path)
        assertEquals("GET", request.method)
    }

    protected fun assertPostRequestSentTo(url: String) {
        val request = server.takeRequest()
        assertEquals(url, request.path)
        assertEquals("POST", request.method)
    }

    protected fun assertPutRequestSentTo(url: String) {
        val request = server.takeRequest()
        assertEquals(url, request.path)
        assertEquals("PUT", request.method)
    }

    protected fun assertDeleteRequestSentTo(url: String) {
        val request = server.takeRequest()
        assertEquals(url, request.path)
        assertEquals("DELETE", request.method)
    }

    protected fun assertRequestContainsHeader(key: String, expectedValue: String) {
        val value = server.takeRequest().getHeader(key)
        assertEquals(expectedValue, value)
    }

    protected fun assertRequestBodyEquals(jsonFile: String) {
        val request = server.takeRequest()
        assertEquals(getContentFromKotlinFile(jsonFile), request.body.readUtf8())
    }
    //endregion

    //region Load file
    private fun getContentFromKotlinFile(fileName: String? = null): String {
        if (fileName == null) {
            return ""
        }

        return javaClass.getResourceAsStream("/$fileName")?.let { getContentFromInputStream(it) }
            ?: ""
    }

    private fun getContentFromAndroidFile(fileName: String? = null): String {
        if (fileName == null) {
            return ""
        }

        return try {
            InstrumentationRegistry.getInstrumentation().context.assets.open(fileName)
                .let { getContentFromInputStream(it) }
        } catch (exception: IOException) {
            "IOException ${exception.message}"
        }
    }

    private fun getContentFromInputStream(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
        }
        return stringBuilder.toString()
    }
    //endregion
}
