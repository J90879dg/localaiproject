package com.localaiproject.android.feature.update

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class DailyUpdateScheduler {
    fun scheduleEveryMidnight(
        context: Context,
        pythonExecutable: String = "python3",
        scriptPath: String? = null,
        workingDirectory: String? = null
    ) {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT)
        val initialDelayMinutes = ChronoUnit.MINUTES.between(now, nextMidnight).coerceAtLeast(1)
        val offlineRoot = File(context.filesDir, "offline_core")
        val resolvedScriptPath = scriptPath
            ?: File(offlineRoot, "tools/run_daily_market_refresh.py").absolutePath
        val resolvedWorkingDirectory = workingDirectory ?: offlineRoot.absolutePath

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val inputData: Data = Data.Builder()
            .putString(DailyUpdateWorker.KEY_PYTHON_EXECUTABLE, pythonExecutable)
            .putString(DailyUpdateWorker.KEY_SCRIPT_PATH, resolvedScriptPath)
            .putString(DailyUpdateWorker.KEY_WORKING_DIRECTORY, resolvedWorkingDirectory)
            .putLong(DailyUpdateWorker.KEY_TIMEOUT_SECONDS, 180L)
            .build()

        val request = PeriodicWorkRequestBuilder<DailyUpdateWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("daily_midnight_refresh")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_midnight_refresh",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
