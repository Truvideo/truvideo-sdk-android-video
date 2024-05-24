package com.truvideo.sdk.video.ui.edit

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.truvideo.sdk.components.button.TruvideoContinueButton
import com.truvideo.sdk.components.icon_button.TruvideoIconButton
import com.truvideo.sdk.video.model.EditMode
import com.truvideo.sdk.video.ui.edit.components.play_button.PlayButton
import com.truvideo.sdk.video.ui.edit.components.tab_bar.TabBar
import com.truvideo.sdk.video.ui.edit.components.timeline.Timeline
import com.truvideo.sdk.video.ui.edit.components.video_preview.VideoPreview
import com.truvideo.sdk.video.ui.edit.components.volume_bar.VolumeBar
import com.truvideo.sdk.video.ui.edit.theme.TruVideoSDKVideoTheme
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Duration.Companion.seconds
import androidx.compose.animation.core.Animatable as AnimateFloat

class TruvideoSdkVideoEditActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoPath: String = intent?.getStringExtra(VIDEO_PATH_EXTRA)!!
        val videoResultPath: String = intent?.getStringExtra(VIDEO_RESULT_PATH_EXTRA)!!

        setContent {
            TruVideoSDKVideoTheme {
                val viewModel = remember { TruvideoSdkVideoViewModel(videoPath, videoResultPath) }
                Content(viewModel)
            }
        }
    }

    companion object {
        const val VIDEO_PATH_EXTRA = "video_path_extra"
        const val VIDEO_RESULT_PATH_EXTRA = "video_result_path_extra"
    }

    @OptIn(UnstableApi::class)
    @Composable
    private fun Content(viewModel: TruvideoSdkVideoViewModel) {
        val videoPath = viewModel.videoPath
        val context = LocalContext.current
        val videoInfo by viewModel.videoInfo.collectAsState()
        val start by viewModel.start.collectAsState()
        val end by viewModel.end.collectAsState()
        val isInitializing by viewModel.isInitializing.collectAsState()
        val isProcessing by viewModel.isProcessing.collectAsState()
        val resultPath by viewModel.resultPath.collectAsState()
        val errorMessage by viewModel.errorResult.collectAsState()
        val volume by viewModel.volume.collectAsState()
        val editMode by viewModel.editMode.collectAsState()
        val videoRotation by viewModel.videoRotation.collectAsState()
        val videoFullScreen by remember { mutableStateOf(false) }

        val exoPlayer = remember(context, videoPath) {
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.fromFile(File(videoPath)))
                .build()

            ExoPlayer.Builder(context)
                .build()
                .apply {
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = false
                    repeatMode = Player.REPEAT_MODE_OFF
                    setSeekParameters(SeekParameters.CLOSEST_SYNC)
                }
        }
        DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

        var isPlaying by remember { mutableStateOf(false) }
        LaunchedEffect(true) { viewModel.init(context) }

        DisposableEffect(Unit) {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(value: Boolean) {
                    isPlaying = value
                }
            }
            exoPlayer.addListener(listener)
            onDispose { exoPlayer.removeListener(listener) }
        }

        var playerPosition by remember { mutableLongStateOf(0L) }
        LaunchedEffect(Unit) {
            while (true) {
                playerPosition = exoPlayer.currentPosition
                val rest = 1.seconds / 30
                delay(rest)
            }
        }

        if (isPlaying) {
            LaunchedEffect(Unit) {
                while (isPlaying) {
                    val rest = 1.seconds / 30
                    val position = exoPlayer.currentPosition
                    if (position >= end) {
                        exoPlayer.pause()
                        break
                    }

                    delay(rest)
                }
            }
        }

        fun calculateVolumeOffset(): Float {
            if (editMode == EditMode.Sound) {
                return 0.0f
            }

            return 100f
        }

        val volumeHorizontalOffset = remember { AnimateFloat(calculateVolumeOffset()) }
        LaunchedEffect(editMode) { volumeHorizontalOffset.animateTo(calculateVolumeOffset()) }


        // Success state
        LaunchedEffect(resultPath) {
            if (resultPath != null) {
                val intent = Intent()
                intent.putExtra("video", resultPath)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        // Error state
        LaunchedEffect(errorMessage) {
            if (errorMessage != null) {
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage(errorMessage ?: "")
                    .setPositiveButton("Accept") { _, _ ->

                    }
                alertDialog.show()
            }
        }

        LaunchedEffect(volume) {

            exoPlayer.volume = volume
        }

        fun onPlayPauseButtonPressed() {
            if (isPlaying) {
                exoPlayer.pause()
            } else {
                val position = exoPlayer.currentPosition
                val atEnd = position >= end
                if (atEnd) {
                    exoPlayer.seekTo(start)
                }

                exoPlayer.play()
            }
        }

        Box(modifier = Modifier.background(color = Color.Black)) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TruvideoIconButton(
                        enabled = !isProcessing && !isInitializing,
                        imageVector = Icons.Default.Close,
                        onPressed = {
                            setResult(RESULT_CANCELED, intent)
                            finish()
                        }
                    )

                    Box(modifier = Modifier.weight(1f))

                    TruvideoContinueButton(
                        enabled = !isProcessing && !isInitializing,
                        onPressed = { viewModel.trimVideo() },
                    )
                }

                // Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VideoPreview(
                        exoPlayer = exoPlayer,
                        rotation = videoRotation,
                        fullScreen = videoFullScreen
                    )

                    // Play/pause
                    PlayButton(
                        isPlaying = isPlaying,
                        onPressed = ::onPlayPauseButtonPressed
                    )

                    // Volume indicator

                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = volumeHorizontalOffset.value.dp)
                    ) {
                        VolumeBar(
                            value = volume,
                            onChanged = { viewModel.updateVolume(it) }
                        )
                    }
                }


                // Panel
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize { _, _ -> }) {
                    AnimatedContent(
                        targetState = editMode,
                        label = "tab-content"
                    ) { mode ->
                        when (mode) {
                            EditMode.Rotation -> Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.align(Alignment.Center),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TruvideoIconButton(
                                        imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                                        onPressed = { viewModel.updateRotation(-90f) }
                                    )

                                    Box(Modifier.width(16.dp))

                                    TruvideoIconButton(
                                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                        onPressed = { viewModel.updateRotation(90f) }
                                    )
                                }

                            }

                            EditMode.Sound -> Box(
                                modifier = Modifier
                                    .height(0.dp)
                                    .fillMaxWidth()
                            )

                            EditMode.Trimming -> Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                val videoFrames by viewModel.videoFrames.collectAsState()

                                Timeline(
                                    enabled = !isProcessing && (editMode == EditMode.Trimming),
                                    start = start,
                                    end = end,
                                    duration = videoInfo?.durationMillis?.toLong() ?: 0,
                                    position = playerPosition,
                                    positionVisible = isPlaying,
                                    itemCount = viewModel.previewItemCount,
                                    frames = videoFrames,
                                    onStartChanged = { position, _ ->
                                        viewModel.updateStart(position)
                                        if (exoPlayer.isPlaying) {
                                            exoPlayer.pause()
                                        }

                                        exoPlayer.seekTo(position)
                                    },
                                    onEndChanged = { position, fromCenter ->
                                        viewModel.updateEnd(position)
                                        if (exoPlayer.isPlaying) {
                                            exoPlayer.pause()
                                        }

                                        if (!fromCenter) {
                                            exoPlayer.seekTo(position)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // App bar
                TabBar(
                    mode = editMode,
                    onEditionModeChange = { viewModel.updateEditionMode(it) }
                )
            }
        }
    }
}