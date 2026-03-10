package com.localaiproject.windows.feature.update

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

data class WindowsUpdateExecutionResult(
    val success: Boolean,
    val exitCode: Int,
    val output: String
)

class WindowsDailyUpdateRunner {
    fun runNow(
        apiBaseUrl: String,
        pythonExecutable: String,
        scriptPath: String,
        workingDirectory: String,
        timeoutSeconds: Long = 180
    ): WindowsUpdateExecutionResult {
        val apiResult = runViaLocalApi(apiBaseUrl)
        if (apiResult.success) {
            return apiResult
        }

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
                output = "Failed API and process update execution: ${exception.message}"
            )
        }
    }

    private fun runViaLocalApi(apiBaseUrl: String): WindowsUpdateExecutionResult {
        return try {
            val endpoint = URL("${apiBaseUrl.trimEnd('/')}/api/daily-refresh")
            val connection = endpoint.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 2500
            connection.readTimeout = 2500
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { output ->
                output.write("""{"force":false}""".toByteArray(Charsets.UTF_8))
            }
            val status = connection.responseCode
            val body = if (status in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            WindowsUpdateExecutionResult(
                success = status in 200..299,
                exitCode = status,
                output = body.ifBlank { "Local API refresh call completed with code=$status" }
            )
        } catch (exception: Exception) {
            WindowsUpdateExecutionResult(
                success = false,
                exitCode = -1,
                output = "Local API refresh unavailable: ${exception.message}"
            )
        }
    }
}
