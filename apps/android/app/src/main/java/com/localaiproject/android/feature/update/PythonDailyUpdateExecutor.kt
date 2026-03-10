package com.localaiproject.android.feature.update

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

data class UpdateExecutionConfig(
    val apiBaseUrl: String,
    val pythonExecutable: String,
    val scriptPath: String,
    val workingDirectory: String,
    val timeoutSeconds: Long = 180L
)

data class UpdateExecutionResult(
    val success: Boolean,
    val exitCode: Int,
    val output: String
)

class PythonDailyUpdateExecutor {
    fun execute(config: UpdateExecutionConfig): UpdateExecutionResult {
        val apiResult = executeViaLocalApi(config)
        if (apiResult.success) {
            return apiResult
        }

        val command = listOf(config.pythonExecutable, config.scriptPath)
        return try {
            val process = ProcessBuilder(command)
                .directory(File(config.workingDirectory))
                .redirectErrorStream(true)
                .start()
            val completed = process.waitFor(config.timeoutSeconds, TimeUnit.SECONDS)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            if (!completed) {
                process.destroyForcibly()
                UpdateExecutionResult(
                    success = false,
                    exitCode = -1,
                    output = "Daily refresh timed out after ${config.timeoutSeconds}s\n$output"
                )
            } else {
                val exit = process.exitValue()
                UpdateExecutionResult(
                    success = exit == 0,
                    exitCode = exit,
                    output = output
                )
            }
        } catch (exception: Exception) {
            UpdateExecutionResult(
                success = false,
                exitCode = -1,
                output = "Failed API and command refresh paths: ${exception.message}"
            )
        }
    }

    private fun executeViaLocalApi(config: UpdateExecutionConfig): UpdateExecutionResult {
        return try {
            val endpoint = URL("${config.apiBaseUrl.trimEnd('/')}/api/daily-refresh")
            val connection = endpoint.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { output ->
                output.write("""{"force":false}""".toByteArray(Charsets.UTF_8))
            }
            val code = connection.responseCode
            val body = if (code in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            UpdateExecutionResult(
                success = code in 200..299,
                exitCode = code,
                output = body.ifBlank { "Local API refresh call completed with code=$code" }
            )
        } catch (exception: Exception) {
            UpdateExecutionResult(
                success = false,
                exitCode = -1,
                output = "Local API refresh unavailable: ${exception.message}"
            )
        }
    }
}
