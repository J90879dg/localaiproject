package com.localaiproject.android.feature.vibematch.domain

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.localaiproject.android.feature.vibematch.model.Mood
import com.localaiproject.android.feature.vibematch.model.Pace
import com.localaiproject.android.feature.vibematch.model.SceneType
import com.localaiproject.android.feature.vibematch.model.VideoAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class HeuristicVideoAnalysisEngine : VideoAnalysisEngine {
    override suspend fun analyzeVideo(
        context: Context,
        videoUri: Uri,
        durationMs: Long
    ): VideoAnalysis = withContext(Dispatchers.Default) {
        val pathHint = videoUri.toString().lowercase()
        val retriever = MediaMetadataRetriever()
        val hashSeed = try {
            retriever.setDataSource(context, videoUri)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull() ?: 0
            abs(videoUri.toString().hashCode() + width + height + rotation + durationMs.toInt())
        } catch (_: Throwable) {
            abs(videoUri.toString().hashCode() + durationMs.toInt())
        } finally {
            retriever.release()
        }

        val pace = when {
            durationMs <= 15_000L -> Pace.FAST
            durationMs <= 35_000L -> Pace.MEDIUM
            else -> Pace.SLOW
        }

        val sceneType = when {
            listOf("nature", "forest", "beach", "mountain", "park").any(pathHint::contains) -> SceneType.NATURE
            listOf("city", "street", "urban", "night").any(pathHint::contains) -> SceneType.URBAN
            listOf("vlog", "selfie", "daily").any(pathHint::contains) -> SceneType.VLOG
            listOf("fight", "sports", "action", "dance").any(pathHint::contains) -> SceneType.ACTION
            listOf("memory", "wedding", "family", "love").any(pathHint::contains) -> SceneType.EMOTIONAL
            else -> SceneType.entries[hashSeed % SceneType.entries.size]
        }

        val mood = when {
            sceneType == SceneType.ACTION && pace == Pace.FAST -> Mood.ENERGETIC
            sceneType == SceneType.NATURE && pace == Pace.SLOW -> Mood.CALM
            sceneType == SceneType.EMOTIONAL -> Mood.SAD
            sceneType == SceneType.URBAN && pace == Pace.FAST -> Mood.DARK
            hashSeed % 7 == 0 -> Mood.CINEMATIC
            hashSeed % 5 == 0 -> Mood.HAPPY
            hashSeed % 3 == 0 -> Mood.ENERGETIC
            else -> Mood.CINEMATIC
        }

        val description = "This video is ${mood.name.lowercase()}, ${pace.name.lowercase()}-paced, and ${sceneType.name.lowercase()}."
        VideoAnalysis(
            mood = mood,
            pace = pace,
            sceneType = sceneType,
            description = description
        )
    }
}
