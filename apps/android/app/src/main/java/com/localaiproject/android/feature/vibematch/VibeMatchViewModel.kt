package com.localaiproject.android.feature.vibematch

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.localaiproject.android.feature.vibematch.domain.CatalogMusicMatcher
import com.localaiproject.android.feature.vibematch.domain.HeuristicVideoAnalysisEngine
import com.localaiproject.android.feature.vibematch.domain.MusicMatcher
import com.localaiproject.android.feature.vibematch.domain.VideoAnalysisEngine
import com.localaiproject.android.feature.vibematch.model.MusicTrack
import com.localaiproject.android.feature.vibematch.model.VideoAnalysis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VibeMatchStep {
    UPLOAD,
    PROCESSING,
    RESULTS
}

data class VibeMatchUiState(
    val step: VibeMatchStep = VibeMatchStep.UPLOAD,
    val selectedVideoUri: Uri? = null,
    val durationMs: Long = 0L,
    val analysis: VideoAnalysis? = null,
    val suggestions: List<MusicTrack> = emptyList(),
    val cleanOnly: Boolean = true,
    val isUnderage: Boolean = false,
    val selectedTrackId: String? = null,
    val errorMessage: String? = null,
    val processingMessage: String = "Analyzing mood, pace, and scene..."
)

class VibeMatchViewModel(
    application: Application,
    private val videoAnalysisEngine: VideoAnalysisEngine = HeuristicVideoAnalysisEngine(),
    private val musicMatcher: MusicMatcher = CatalogMusicMatcher()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VibeMatchUiState())
    val uiState: StateFlow<VibeMatchUiState> = _uiState.asStateFlow()

    fun setUnderage(isUnderage: Boolean) {
        _uiState.update { state ->
            state.copy(
                isUnderage = isUnderage,
                cleanOnly = if (isUnderage) true else state.cleanOnly
            )
        }
        refreshSuggestions()
    }

    fun setCleanOnly(enabled: Boolean) {
        _uiState.update { state ->
            if (state.isUnderage) {
                state.copy(cleanOnly = true)
            } else {
                state.copy(cleanOnly = enabled)
            }
        }
        refreshSuggestions()
    }

    fun selectTrack(trackId: String) {
        _uiState.update { it.copy(selectedTrackId = trackId) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetToUpload() {
        _uiState.update {
            VibeMatchUiState(
                cleanOnly = it.cleanOnly,
                isUnderage = it.isUnderage
            )
        }
    }

    fun onVideoSelected(videoUri: Uri) {
        viewModelScope.launch {
            val inspection = VideoInputValidator.inspect(getApplication(), videoUri)
            val error = VideoInputValidator.validate(inspection)
            if (error != null) {
                _uiState.update {
                    it.copy(
                        errorMessage = error,
                        step = VibeMatchStep.UPLOAD
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    step = VibeMatchStep.PROCESSING,
                    selectedVideoUri = videoUri,
                    durationMs = inspection.durationMs,
                    errorMessage = null,
                    analysis = null,
                    suggestions = emptyList()
                )
            }

            // MVP target keeps analysis under ~10s for good UX.
            delay(1200)
            val analysis = videoAnalysisEngine.analyzeVideo(
                context = getApplication(),
                videoUri = videoUri,
                durationMs = inspection.durationMs
            )
            val suggestions = musicMatcher.suggestTracks(
                analysis = analysis,
                cleanOnly = _uiState.value.cleanOnly,
                maxResults = 8
            )

            _uiState.update {
                it.copy(
                    step = VibeMatchStep.RESULTS,
                    analysis = analysis,
                    suggestions = suggestions,
                    selectedTrackId = suggestions.firstOrNull()?.id
                )
            }
        }
    }

    fun reportPreviewError() {
        _uiState.update {
            it.copy(errorMessage = "Unable to load this preview track. Please pick another.")
        }
    }

    private fun refreshSuggestions() {
        val state = _uiState.value
        val analysis = state.analysis ?: return
        val refreshed = musicMatcher.suggestTracks(
            analysis = analysis,
            cleanOnly = state.cleanOnly,
            maxResults = 8
        )
        _uiState.update {
            it.copy(
                suggestions = refreshed,
                selectedTrackId = refreshed.firstOrNull()?.id
            )
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VibeMatchViewModel(application) as T
                }
            }
        }
    }
}
