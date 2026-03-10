package com.localaiproject.android.feature.update

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class DailyUpdateScheduler {
    fun scheduleEveryMidnight(context: Context) {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT)
        val initialDelayMinutes = ChronoUnit.MINUTES.between(now, nextMidnight).coerceAtLeast(1)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<DailyUpdateWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("daily_midnight_refresh")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_midnight_refresh",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
