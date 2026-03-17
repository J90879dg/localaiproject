package com.localaiproject.android.feature.vibematch.domain

import com.localaiproject.android.feature.vibematch.model.MusicTrack
import com.localaiproject.android.feature.vibematch.model.VideoAnalysis

interface MusicMatcher {
    fun suggestTracks(
        analysis: VideoAnalysis,
        cleanOnly: Boolean,
        maxResults: Int = 8
    ): List<MusicTrack>
}
