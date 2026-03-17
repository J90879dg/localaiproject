package com.localaiproject.android.feature.vibematch.ui

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CaptureVideo
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localaiproject.android.feature.vibematch.VibeMatchStep
import com.localaiproject.android.feature.vibematch.VibeMatchUiState
import com.localaiproject.android.feature.vibematch.VibeMatchViewModel
import com.localaiproject.android.feature.vibematch.model.MusicTrack
import com.localaiproject.android.ui.theme.ElectricBlue
import com.localaiproject.android.ui.theme.GlassWhite
import com.localaiproject.android.ui.theme.MidnightNavy
import com.localaiproject.android.ui.theme.NeonCyan
import com.localaiproject.android.ui.theme.SoftViolet
import java.io.File

@Composable
fun VibeMatchScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: VibeMatchViewModel = viewModel(
        factory = VibeMatchViewModel.provideFactory(application)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pickVideoLauncher = rememberLauncherForActivityResult(GetContent()) { uri ->
        if (uri != null) viewModel.onVideoSelected(uri)
    }

    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    val captureVideoLauncher = rememberLauncherForActivityResult(CaptureVideo()) { success ->
        if (success) {
            pendingCaptureUri?.let(viewModel::onVideoSelected)
        }
    }

    VibeMatchContent(
        uiState = uiState,
        onUploadClick = { pickVideoLauncher.launch("video/*") },
        onRecordClick = {
            val uri = createTempVideoUri(application)
            pendingCaptureUri = uri
            captureVideoLauncher.launch(uri)
        },
        onCleanOnlyChanged = viewModel::setCleanOnly,
        onUnderageChanged = viewModel::setUnderage,
        onTrackSelected = viewModel::selectTrack,
        onPreviewError = viewModel::reportPreviewError,
        onDismissError = viewModel::clearError,
        onTryAnotherVideo = viewModel::resetToUpload
    )
}

@Composable
private fun VibeMatchContent(
    uiState: VibeMatchUiState,
    onUploadClick: () -> Unit,
    onRecordClick: () -> Unit,
    onCleanOnlyChanged: (Boolean) -> Unit,
    onUnderageChanged: (Boolean) -> Unit,
    onTrackSelected: (String) -> Unit,
    onPreviewError: () -> Unit,
    onDismissError: () -> Unit,
    onTryAnotherVideo: () -> Unit
) {
    val background = Brush.verticalGradient(
        listOf(MidnightNavy, ElectricBlue.copy(alpha = 0.35f), SoftViolet.copy(alpha = 0.2f))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "VibeMatch",
            style = MaterialTheme.typography.headlineMedium,
            color = GlassWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Upload a video and match the perfect vibe-safe soundtrack.",
            color = NeonCyan
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onUploadClick, modifier = Modifier.weight(1f)) {
                Text("Upload Video")
            }
            OutlinedButton(onClick = onRecordClick, modifier = Modifier.weight(1f)) {
                Text("Record Video")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Clean Only", color = GlassWhite, fontWeight = FontWeight.SemiBold)
                Text("Strict family-friendly tracks", color = GlassWhite.copy(alpha = 0.8f))
            }
            Switch(
                checked = uiState.cleanOnly,
                enabled = !uiState.isUnderage,
                onCheckedChange = onCleanOnlyChanged
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Under 18", color = GlassWhite, fontWeight = FontWeight.SemiBold)
            Switch(checked = uiState.isUnderage, onCheckedChange = onUnderageChanged)
        }

        if (uiState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftViolet.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(uiState.errorMessage, color = GlassWhite)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(onClick = onDismissError) {
                        Text("Dismiss")
                    }
                }
            }
        }

        AnimatedContent(targetState = uiState.step, label = "vibematch-step") { step ->
            when (step) {
                VibeMatchStep.UPLOAD -> UploadStep(videoUri = uiState.selectedVideoUri)
                VibeMatchStep.PROCESSING -> ProcessingStep(videoUri = uiState.selectedVideoUri)
                VibeMatchStep.RESULTS -> ResultsStep(
                    uiState = uiState,
                    onTrackSelected = onTrackSelected,
                    onPreviewError = onPreviewError,
                    onTryAnotherVideo = onTryAnotherVideo
                )
            }
        }
    }
}

@Composable
private fun UploadStep(videoUri: Uri?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VideoPreview(videoUri = videoUri, modifier = Modifier.fillMaxWidth().height(260.dp))
        Text(
            text = "Supported formats: MP4, MOV. Maximum length: 60 seconds.",
            color = GlassWhite.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun ProcessingStep(videoUri: Uri?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VideoPreview(videoUri = videoUri, modifier = Modifier.fillMaxWidth().height(260.dp))
        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(36.dp))
        Text("Analyzing mood, pace, and scene...", color = GlassWhite)
        Text("Target processing time: under 10 seconds", color = GlassWhite.copy(alpha = 0.75f))
    }
}

@Composable
private fun ResultsStep(
    uiState: VibeMatchUiState,
    onTrackSelected: (String) -> Unit,
    onPreviewError: () -> Unit,
    onTryAnotherVideo: () -> Unit
) {
    val selectedTrack = uiState.suggestions.firstOrNull { it.id == uiState.selectedTrackId }
    val context = LocalContext.current
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    val stopPlayback = remember(mediaPlayer, videoViewRef) {
        {
            mediaPlayer?.let {
                runCatching { it.stop() }
                it.release()
            }
            mediaPlayer = null
            videoViewRef?.pause()
            videoViewRef?.seekTo(0)
            isPreviewPlaying = false
        }
    }

    DisposableEffect(uiState.selectedVideoUri, uiState.selectedTrackId) {
        onDispose { stopPlayback() }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SyncPreviewVideo(
            videoUri = uiState.selectedVideoUri,
            modifier = Modifier.fillMaxWidth().height(250.dp),
            onVideoViewReady = { videoViewRef = it },
            onVideoEnded = { isPreviewPlaying = false }
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = GlassWhite.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("AI Analysis", color = NeonCyan, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.analysis?.description ?: "No analysis available.",
                    color = GlassWhite
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    val previewTrack = selectedTrack ?: return@Button
                    val videoView = videoViewRef ?: return@Button
                    stopPlayback()

                    try {
                        val player = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            setDataSource(context, Uri.parse(previewTrack.previewUrl))
                            setOnPreparedListener {
                                videoView.seekTo(0)
                                it.seekTo(0)
                                videoView.start()
                                it.start()
                                isPreviewPlaying = true
                            }
                            setOnCompletionListener {
                                isPreviewPlaying = false
                            }
                            prepareAsync()
                        }
                        mediaPlayer = player
                    } catch (_: Throwable) {
                        onPreviewError()
                    }
                },
                enabled = selectedTrack != null
            ) {
                Text(if (isPreviewPlaying) "Restart Preview" else "Play Sync Preview")
            }
            OutlinedButton(onClick = onTryAnotherVideo) {
                Text("Use Another Video")
            }
        }

        Text(
            text = "Music suggestions (${uiState.suggestions.size})",
            color = GlassWhite,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.suggestions.forEach { track ->
                TrackCard(
                    track = track,
                    isSelected = track.id == uiState.selectedTrackId,
                    onSelect = { onTrackSelected(track.id) }
                )
            }
        }

        Text(
            text = "This app provides music suggestions only. Users are responsible for how music is used. The app is not liable for copyright issues or misuse.",
            color = GlassWhite.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TrackCard(
    track: MusicTrack,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) NeonCyan else GlassWhite.copy(alpha = 0.35f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = GlassWhite.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(track.title, color = GlassWhite, fontWeight = FontWeight.Bold)
            Text("Genre: ${track.genre}", color = GlassWhite.copy(alpha = 0.9f))
            Text("Mood: ${track.moodTag.name.lowercase()}", color = GlassWhite.copy(alpha = 0.9f))
            Text("Tempo: ${track.tempo.name.lowercase()}", color = GlassWhite.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun VideoPreview(
    videoUri: Uri?,
    modifier: Modifier
) {
    if (videoUri == null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(GlassWhite.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Video preview appears here", color = GlassWhite.copy(alpha = 0.8f))
        }
        return
    }

    AndroidView(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite.copy(alpha = 0.08f)),
        factory = { context ->
            VideoView(context).apply {
                setOnPreparedListener { player ->
                    player.isLooping = true
                    seekTo(10)
                }
            }
        },
        update = { view ->
            view.setVideoURI(videoUri)
            view.seekTo(10)
        }
    )
}

@Composable
private fun SyncPreviewVideo(
    videoUri: Uri?,
    modifier: Modifier,
    onVideoViewReady: (VideoView) -> Unit,
    onVideoEnded: () -> Unit
) {
    if (videoUri == null) {
        VideoPreview(videoUri = null, modifier = modifier)
        return
    }
    AndroidView(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite.copy(alpha = 0.08f)),
        factory = { context ->
            VideoView(context).apply {
                setOnCompletionListener { onVideoEnded() }
                setVideoURI(videoUri)
                seekTo(10)
                onVideoViewReady(this)
            }
        },
        update = { view ->
            onVideoViewReady(view)
        }
    )
}

private fun createTempVideoUri(application: Application): Uri {
    val folder = File(application.cacheDir, "captured_videos").apply { mkdirs() }
    val file = File.createTempFile("vibematch_capture_", ".mp4", folder)
    return FileProvider.getUriForFile(
        application,
        "${application.packageName}.fileprovider",
        file
    )
}
