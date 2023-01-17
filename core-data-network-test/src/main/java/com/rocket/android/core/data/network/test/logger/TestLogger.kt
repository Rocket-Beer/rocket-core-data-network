package com.rocket.android.core.data.network.test.logger

import com.rocket.core.crashreporting.logger.CrashLogger
import com.rocket.core.crashreporting.logger.LogLevel
import java.io.File

class TestLogger : CrashLogger {

    override fun log(message: String, map: Map<String, String?>, logLevel: LogLevel, logPath: File?) =
        println("[${logLevel.name.first().titlecase()}] TestLogger: $message - $map")

    override fun log(exception: Throwable, map: Map<String, String?>, logLevel: LogLevel, logPath: File?) =
        println("[${logLevel.name.first().titlecase()}] TestLogger: ${exception.message} - $map")
}
