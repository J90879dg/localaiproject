package com.localaiproject.android.feature.vibematch.domain

import android.content.Context
import android.net.Uri
import com.localaiproject.android.feature.vibematch.model.VideoAnalysis

interface VideoAnalysisEngine {
    suspend fun analyzeVideo(
        context: Context,
        videoUri: Uri,
        durationMs: Long
    ): VideoAnalysis
}
