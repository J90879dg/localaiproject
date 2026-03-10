package com.localaiproject.android.feature.update

import java.io.File
import java.util.concurrent.TimeUnit

data class UpdateExecutionConfig(
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
                output = "Failed to run daily refresh command: ${exception.message}"
            )
        }
    }
}
