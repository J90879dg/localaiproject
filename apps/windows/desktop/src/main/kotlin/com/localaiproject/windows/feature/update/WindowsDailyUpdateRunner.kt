package com.localaiproject.windows.feature.update

import java.io.File
import java.util.concurrent.TimeUnit

data class WindowsUpdateExecutionResult(
    val success: Boolean,
    val exitCode: Int,
    val output: String
)

class WindowsDailyUpdateRunner {
    fun runNow(
        pythonExecutable: String,
        scriptPath: String,
        workingDirectory: String,
        timeoutSeconds: Long = 180
    ): WindowsUpdateExecutionResult {
        val command = listOf(pythonExecutable, scriptPath)
        return try {
            val process = ProcessBuilder(command)
                .directory(File(workingDirectory))
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            if (!finished) {
                process.destroyForcibly()
                WindowsUpdateExecutionResult(
                    success = false,
                    exitCode = -1,
                    output = "Daily update timed out after ${timeoutSeconds}s.\n$output"
                )
            } else {
                val exitCode = process.exitValue()
                WindowsUpdateExecutionResult(
                    success = exitCode == 0,
                    exitCode = exitCode,
                    output = output
                )
            }
        } catch (exception: Exception) {
            WindowsUpdateExecutionResult(
                success = false,
                exitCode = -1,
                output = "Failed to execute update process: ${exception.message}"
            )
        }
    }
}
