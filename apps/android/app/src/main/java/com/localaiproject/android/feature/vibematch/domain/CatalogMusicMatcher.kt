package com.localaiproject.android.feature.vibematch.domain

import com.localaiproject.android.feature.vibematch.model.Mood
import com.localaiproject.android.feature.vibematch.model.MusicTrack
import com.localaiproject.android.feature.vibematch.model.Pace
import com.localaiproject.android.feature.vibematch.model.SceneType
import com.localaiproject.android.feature.vibematch.model.VideoAnalysis
import kotlin.math.max

class CatalogMusicMatcher : MusicMatcher {
    override fun suggestTracks(
        analysis: VideoAnalysis,
        cleanOnly: Boolean,
        maxResults: Int
    ): List<MusicTrack> {
        val filtered = catalog
            .asSequence()
            .filter { it.isRoyaltyFree }
            .filter { !cleanOnly || it.isClean }
            .sortedByDescending { scoreTrack(it, analysis) }
            .toList()

        val safeCount = max(5, maxResults.coerceAtMost(10))
        return filtered.take(safeCount)
    }

    private fun scoreTrack(track: MusicTrack, analysis: VideoAnalysis): Int {
        var score = 0
        if (track.moodTag == analysis.mood) score += 5
        if (track.tempo == analysis.pace) score += 4
        if (analysis.sceneType == SceneType.NATURE && track.genre.contains("ambient", true)) score += 3
        if (analysis.sceneType == SceneType.ACTION && track.genre.contains("electronic", true)) score += 3
        if (analysis.sceneType == SceneType.EMOTIONAL && track.genre.contains("piano", true)) score += 3
        if (analysis.sceneType == SceneType.URBAN && track.genre.contains("lofi", true)) score += 2
        if (track.isClean) score += 1
        return score
    }

    companion object {
        // Pixabay preview links are used for MVP-safe streaming previews.
        val catalog: List<MusicTrack> = listOf(
            MusicTrack("t1", "Neon Pulse", "Electronic", Mood.ENERGETIC, Pace.FAST, "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8b35f6f52.mp3", true, true),
            MusicTrack("t2", "Cinematic Rise", "Cinematic", Mood.CINEMATIC, Pace.MEDIUM, "https://cdn.pixabay.com/download/audio/2023/06/28/audio_2b8bb91057.mp3", true, true),
            MusicTrack("t3", "City Night Drift", "LoFi", Mood.DARK, Pace.MEDIUM, "https://cdn.pixabay.com/download/audio/2022/11/22/audio_12f9c8f658.mp3", true, true),
            MusicTrack("t4", "Sunlit Steps", "Pop", Mood.HAPPY, Pace.FAST, "https://cdn.pixabay.com/download/audio/2022/10/25/audio_946f94f7f8.mp3", true, true),
            MusicTrack("t5", "Faded Memories", "Piano", Mood.SAD, Pace.SLOW, "https://cdn.pixabay.com/download/audio/2022/08/04/audio_4b4c380ce5.mp3", true, true),
            MusicTrack("t6", "Quiet Horizon", "Ambient", Mood.CALM, Pace.SLOW, "https://cdn.pixabay.com/download/audio/2021/09/06/audio_b03a2a98ab.mp3", true, true),
            MusicTrack("t7", "Motion Blur", "Electronic", Mood.ENERGETIC, Pace.FAST, "https://cdn.pixabay.com/download/audio/2022/03/10/audio_1d47f63de8.mp3", true, true),
            MusicTrack("t8", "Urban Clouds", "LoFi", Mood.CINEMATIC, Pace.MEDIUM, "https://cdn.pixabay.com/download/audio/2022/06/16/audio_d5d3ed9d6f.mp3", true, true),
            MusicTrack("t9", "Trail of Light", "Ambient", Mood.CALM, Pace.MEDIUM, "https://cdn.pixabay.com/download/audio/2022/05/16/audio_d7f2f43f40.mp3", true, true),
            MusicTrack("t10", "Morning Vlog", "Indie Pop", Mood.HAPPY, Pace.MEDIUM, "https://cdn.pixabay.com/download/audio/2022/01/26/audio_d172f7f03f.mp3", true, true),
            MusicTrack("t11", "Deep Focus Bass", "Electronic", Mood.DARK, Pace.FAST, "https://cdn.pixabay.com/download/audio/2023/04/14/audio_5f2f5670ad.mp3", true, true),
            MusicTrack("t12", "Family Montage", "Piano", Mood.CINEMATIC, Pace.SLOW, "https://cdn.pixabay.com/download/audio/2022/07/31/audio_06b65e5221.mp3", true, true)
        )
    }
}
