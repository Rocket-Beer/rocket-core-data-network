package com.rocket.android.core.data.network.test.coroutines

import com.rocket.android.core.data.network.test.logger.TestLogger
import com.rocket.core.crashreporting.logger.CrashLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@Suppress("unused")
@ExperimentalCoroutinesApi
open class BaseCoroutinesTest(private val crashLogger: CrashLogger = TestLogger()) {
    private val testDispatcher = TestCoroutineDispatcher()
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        crashLogger.log("BaseCoroutinesTest - CoroutineExceptionHandler handled crash $exception \\n ${exception.cause?.message}\"")
    }
    protected val testCoroutineScope =
        TestCoroutineScope(testDispatcher + coroutineExceptionHandler)

    @Before
    fun setupCoroutines() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun resetCoroutines() {
        Dispatchers.resetMain()
        testCoroutineScope.cleanupTestCoroutines()
    }
}
