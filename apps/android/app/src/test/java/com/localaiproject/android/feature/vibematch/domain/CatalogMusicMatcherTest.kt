package com.localaiproject.android.feature.vibematch.domain

import com.localaiproject.android.feature.vibematch.model.Mood
import com.localaiproject.android.feature.vibematch.model.Pace
import com.localaiproject.android.feature.vibematch.model.SceneType
import com.localaiproject.android.feature.vibematch.model.VideoAnalysis
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogMusicMatcherTest {

    private val matcher = CatalogMusicMatcher()

    @Test
    fun `returns between five and ten safe tracks`() {
        val analysis = VideoAnalysis(
            mood = Mood.ENERGETIC,
            pace = Pace.FAST,
            sceneType = SceneType.ACTION,
            description = "Energetic, fast, and action."
        )

        val results = matcher.suggestTracks(analysis = analysis, cleanOnly = true, maxResults = 8)
        assertTrue(results.size in 5..10)
        assertTrue(results.all { it.isRoyaltyFree && it.isClean })
    }

    @Test
    fun `clean filter still enforced for underage-like mode`() {
        val analysis = VideoAnalysis(
            mood = Mood.DARK,
            pace = Pace.MEDIUM,
            sceneType = SceneType.URBAN,
            description = "Urban and dark."
        )

        val results = matcher.suggestTracks(analysis = analysis, cleanOnly = true, maxResults = 10)
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.isClean })
    }
}
