package com.localaiproject.android.feature.vibematch

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toFile

data class VideoInspection(
    val mimeType: String?,
    val durationMs: Long,
    val extension: String?
)

object VideoInputValidator {
    private val supportedMimeTypes = setOf("video/mp4", "video/quicktime")
    private val supportedExtensions = setOf("mp4", "mov")
    private const val maxDurationMs = 60_000L

    fun inspect(context: Context, uri: Uri): VideoInspection {
        val mimeType = context.contentResolver.getType(uri)
        val extension = runCatching { uri.toFile().extension.lowercase() }.getOrNull()
            ?: uri.lastPathSegment
                ?.substringAfterLast('.', "")
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }

        val retriever = MediaMetadataRetriever()
        val durationMs = try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
        } catch (_: Throwable) {
            0L
        } finally {
            retriever.release()
        }

        return VideoInspection(mimeType = mimeType, durationMs = durationMs, extension = extension)
    }

    fun validate(inspection: VideoInspection): String? {
        val mimeSupported = inspection.mimeType?.let { it in supportedMimeTypes } ?: false
        val extensionSupported = inspection.extension?.let { it in supportedExtensions } ?: false
        if (!mimeSupported && !extensionSupported) {
            return "Only MP4 or MOV videos are supported."
        }
        if (inspection.durationMs <= 0L) {
            return "Unable to read video duration. Please try another video."
        }
        if (inspection.durationMs > maxDurationMs) {
            return "Video must be 60 seconds or less for fast processing."
        }
        return null
    }
}
