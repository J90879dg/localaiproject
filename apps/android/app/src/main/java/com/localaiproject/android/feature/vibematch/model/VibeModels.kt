package com.localaiproject.android.feature.vibematch.model

enum class Mood {
    HAPPY,
    SAD,
    ENERGETIC,
    CALM,
    CINEMATIC,
    DARK
}

enum class Pace {
    SLOW,
    MEDIUM,
    FAST
}

enum class SceneType {
    ACTION,
    VLOG,
    NATURE,
    URBAN,
    EMOTIONAL
}

data class VideoAnalysis(
    val mood: Mood,
    val pace: Pace,
    val sceneType: SceneType,
    val description: String
)

data class MusicTrack(
    val id: String,
    val title: String,
    val genre: String,
    val moodTag: Mood,
    val tempo: Pace,
    val previewUrl: String,
    val isRoyaltyFree: Boolean,
    val isClean: Boolean
)
