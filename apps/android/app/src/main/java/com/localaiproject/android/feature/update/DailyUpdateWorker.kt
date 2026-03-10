package com.localaiproject.android.feature.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class DailyUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val executor = PythonDailyUpdateExecutor()

    override suspend fun doWork(): Result {
        val config = UpdateExecutionConfig(
            pythonExecutable = inputData.getString(KEY_PYTHON_EXECUTABLE) ?: "python3",
            scriptPath = inputData.getString(KEY_SCRIPT_PATH)
                ?: "${applicationContext.filesDir.absolutePath}/offline_core/tools/run_daily_market_refresh.py",
            workingDirectory = inputData.getString(KEY_WORKING_DIRECTORY)
                ?: "${applicationContext.filesDir.absolutePath}/offline_core",
            timeoutSeconds = inputData.getLong(KEY_TIMEOUT_SECONDS, 180L)
        )

        val result = executor.execute(config)
        if (result.success) {
            return Result.success(
                workDataOf(
                    KEY_LAST_EXIT_CODE to result.exitCode,
                    KEY_LAST_OUTPUT to result.output.take(800)
                )
            )
        }

        if (runAttemptCount < 3) {
            return Result.retry()
        }
        return Result.failure(
            Data.Builder()
                .putInt(KEY_LAST_EXIT_CODE, result.exitCode)
                .putString(KEY_LAST_OUTPUT, result.output.take(800))
                .build()
        )
    }

    companion object {
        const val KEY_PYTHON_EXECUTABLE = "python_executable"
        const val KEY_SCRIPT_PATH = "script_path"
        const val KEY_WORKING_DIRECTORY = "working_directory"
        const val KEY_TIMEOUT_SECONDS = "timeout_seconds"
        const val KEY_LAST_EXIT_CODE = "last_exit_code"
        const val KEY_LAST_OUTPUT = "last_output"
    }
}
