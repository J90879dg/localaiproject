package com.localaiproject.android.feature.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        // TODO: connect to local core endpoint and trigger:
        // orchestrator.refresh_market_data_if_due(...)
        return Result.success()
    }
}
