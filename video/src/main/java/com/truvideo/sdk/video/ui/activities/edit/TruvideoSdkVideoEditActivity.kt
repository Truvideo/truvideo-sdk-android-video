package com.truvideo.sdk.video.ui.activities.edit

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.truvideo.sdk.components.animated_content.TruvideoAnimatedContent
import com.truvideo.sdk.components.animated_value.animateFloat
import com.truvideo.sdk.components.animated_value.springAnimationFloatSpec
import com.truvideo.sdk.components.button.TruvideoContinueButton
import com.truvideo.sdk.components.button.TruvideoIconButton
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoAuthAdapterImpl
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoLogAdapterImpl
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoVersionPropertiesAdapterImpl
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.ui.activities.edit.components.exit_panel.ExitPanel
import com.truvideo.sdk.video.ui.activities.edit.components.play_button.PlayButton
import com.truvideo.sdk.video.ui.activities.edit.components.tab_bar.TabBar
import com.truvideo.sdk.video.ui.activities.edit.components.timeline.VideoThumbnailPreview
import com.truvideo.sdk.video.ui.activities.edit.components.video_preview.VideoPreview
import com.truvideo.sdk.video.ui.activities.edit.components.volume_bar.VolumeBar
import com.truvideo.sdk.video.ui.activities.edit.theme.TruvideoSdkTheme
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Duration.Companion.seconds

internal class TruvideoSdkVideoEditActivity : ComponentActivity() {

    private lateinit var viewModel: TruvideoSdkVideoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fun showFatalErrorDialog(
            title: String,
            message: String
        ) {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setOnDismissListener {
                    finish()
                }
                .setPositiveButton("Accept") { _, _ ->
                }
                .show()
        }

        // Validate auth
        try {
            val versionPropertiesAdapter = TruvideoSdkVideoVersionPropertiesAdapterImpl(this)
            val authAdapter = TruvideoSdkVideoAuthAdapterImpl(
                logAdapter = TruvideoSdkVideoLogAdapterImpl(
                    versionPropertiesAdapter = versionPropertiesAdapter
                ),
                versionPropertiesAdapter = versionPropertiesAdapter
            )
            authAdapter.validateAuthentication()
        } catch (exception: Exception) {
            showFatalErrorDialog("Error", "Auth required")
            return
        }

        val params = TruvideoSdkVideoEditParams.fromJson(intent?.getStringExtra("params") ?: "")

        setContent {
            val context = LocalContext.current
            val view = LocalView.current

            TruvideoSdkTheme {
                viewModel = remember {
                    TruvideoSdkVideoViewModel(
                        context = context,
                        input = params.input,
                        output = params.output,
                        previewMode = view.isInEditMode
                    )
                }

                Content(viewModel = viewModel)
            }
        }
    }

    @OptIn(UnstableApi::class)
    @Composable
    private fun Content(
        viewModel: TruvideoSdkVideoViewModel
    ) {
        val context = LocalContext.current
        val view = LocalView.current
        var exitPanelVisible by remember { mutableStateOf(false) }
        val videoPath by viewModel.videoPath.collectAsStateWithLifecycle()
        val videoInfo by viewModel.videoInfo.collectAsStateWithLifecycle()
        val start by viewModel.start.collectAsStateWithLifecycle()
        val end by viewModel.end.collectAsStateWithLifecycle()
        val isInitializing by viewModel.isInitializing.collectAsStateWithLifecycle()
        val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
        val isProcessingClearNoise by viewModel.isProcessingClearNoise.collectAsStateWithLifecycle()
        val resultPath by viewModel.resultPath.collectAsStateWithLifecycle()
        val errorMessage by viewModel.errorResult.collectAsStateWithLifecycle()
        val volume by viewModel.volume.collectAsStateWithLifecycle()
        val editMode by viewModel.editMode.collectAsStateWithLifecycle()
        val videoRotation by viewModel.videoRotation.collectAsStateWithLifecycle()
        val withChanges by viewModel.withChanges.collectAsStateWithLifecycle()

        // Init exoplayer
        val exoPlayer = remember(true) {
            if (view.isInEditMode) {
                null
            } else {
                ExoPlayer.Builder(context)
                    .build()
                    .apply {
                        playWhenReady = false
                        repeatMode = Player.REPEAT_MODE_OFF
                        setSeekParameters(SeekParameters.CLOSEST_SYNC)
                    }
            }
        }

        // Change video path
        LaunchedEffect(videoPath) {
            Log.d("TruvideoSdkVideo", "Changing path to $videoPath")
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
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
        }

        // On activity disposed
        DisposableEffect(Unit) {
            onDispose {
                viewModel.close()
                exoPlayer?.release()
            }
        }

        var isPlaying by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { viewModel.init() }

        DisposableEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(value: Boolean) {
                    isPlaying = value
                }
            }
            exoPlayer?.addListener(listener)
            onDispose { exoPlayer?.removeListener(listener) }
        }

        var playerPosition by remember { mutableLongStateOf(0L) }
        LaunchedEffect(exoPlayer, start, end) {
            if (exoPlayer == null) return@LaunchedEffect

            while (true) {
                playerPosition = exoPlayer.currentPosition
                if (playerPosition > end) {
                    exoPlayer.pause()
                }
                val rest = 1.seconds / 30
                delay(rest)
            }
        }

        fun calculateVolumeOffset(): Float {
            if (editMode == EditMode.Sound) {
                return 0.0f
            }

            return 100f
        }

        val volumeHorizontalOffsetAnim = animateFloat(
            value = calculateVolumeOffset(),
            spec = springAnimationFloatSpec
        )

        // Success state
        LaunchedEffect(resultPath) {
            if (resultPath != null) {
                Log.d("TruvideoSdkVideo", "Finish activity $resultPath")
                setResult(RESULT_OK, Intent().apply { putExtra("path", resultPath) })
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
            exoPlayer?.volume = volume
        }

        fun onPlayPauseButtonPressed() {
            if (isPlaying) {
                exoPlayer?.pause()
            } else {
                val position = exoPlayer?.currentPosition ?: 0
                val atEnd = position >= end
                if (atEnd) {
                    exoPlayer?.seekTo(start)
                }

                exoPlayer?.play()
            }
        }

        fun onButtonClosePressed() {
            if (withChanges) {
                exitPanelVisible = true
            } else {
                setResult(RESULT_CANCELED, intent)
                finish()
            }
        }

        BackHandler(true) { onButtonClosePressed() }

        Box(
            modifier = Modifier
                .background(color = Color.Black)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TruvideoIconButton(
                        enabled = !isProcessing && !isInitializing,
                        icon = Icons.Default.Close,
                        small = true,
                        onPressed = { onButtonClosePressed() }
                    )

                    Box(modifier = Modifier.weight(1f))

                    TruvideoContinueButton(
                        enabled = !isProcessing && !isInitializing && !isProcessingClearNoise,
                        small = true,
                        onPressed = { viewModel.apply() },
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

                    val aspectRatio by remember { viewModel.videoAspectRatio }.collectAsStateWithLifecycle()

                    VideoPreview(
                        exoPlayer = exoPlayer,
                        rotation = videoRotation,
                        aspectRatio = aspectRatio,
                        fullScreen = false
                    )

                    // Play/pause
                    PlayButton(
                        isPlaying = isPlaying,
                        onPressed = { onPlayPauseButtonPressed() }
                    )

                    // Volume indicator
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = volumeHorizontalOffsetAnim.dp)
                    ) {
                        VolumeBar(
                            value = volume,
                            onChanged = { viewModel.updateVolume(it) }
                        )
                    }
                }


                // Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                ) {
                    TruvideoAnimatedContent(
                        targetState = editMode,
                    ) { editModeTarget ->
                        when (editModeTarget) {
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
                                        icon = Icons.AutoMirrored.Filled.RotateLeft,
                                        small = true,
                                        onPressed = { viewModel.updateRotation(-90f) }
                                    )

                                    Box(Modifier.width(8.dp))

                                    TruvideoIconButton(
                                        icon = Icons.AutoMirrored.Filled.RotateRight,
                                        small = true,
                                        onPressed = { viewModel.updateRotation(90f) }
                                    )
                                }
                            }

                            EditMode.Sound -> Box(modifier = Modifier.fillMaxWidth()) {}
//                            TabSoundOptions(
//                                useNoiseCanceledVideo = useNoiseCanceledVideo,
//                                isProcessingClearNoise = isProcessingClearNoise,
//                                onUseNoiseCanceledVideoPressed = { viewModel.toggleUseNoiseCanceledVideo() }
//                            )

                            EditMode.Trimming -> Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                val videoFrames by viewModel.videoFrames.collectAsState()

                                VideoThumbnailPreview(
                                    enabled = !isProcessing && !isInitializing,
                                    itemCount = viewModel.previewItemCount,
                                    duration = videoInfo?.durationMillis?.toFloat() ?: 0f,
                                    start = start.toFloat(),
                                    end = end.toFloat(),
                                    position = playerPosition.toFloat(),
                                    positionVisible = isPlaying,
                                    updateStart = { value, _ ->
                                        Log.d("TruvideoSdkVideo", "Update start $value")
                                        viewModel.updateStart(value.toLong())
                                        if (exoPlayer?.isPlaying == true) exoPlayer.pause()
                                        exoPlayer?.seekTo(value.toLong())
                                    },
                                    updateEnd = { value, fromCenter ->
                                        Log.d("TruvideoSdkVideo", "Update end $value")
                                        viewModel.updateEnd(value.toLong())
                                        if (exoPlayer?.isPlaying == true) exoPlayer.pause()
                                        if (!fromCenter) exoPlayer?.seekTo(value.toLong())
                                    }
                                ) { index ->
                                    val frame = try {
                                        videoFrames[index]
                                    } catch (_: Exception) {
                                        null
                                    }
                                    TruvideoAnimatedContent(targetState = frame) { frameTarget ->
                                        if (frameTarget?.bitmap != null) {
                                            Image(
                                                frameTarget.bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center
                                            )
                                        } else {
                                            Box(Modifier.fillMaxSize())
                                        }
                                    }
                                }

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

        ExitPanel(
            visible = exitPanelVisible,
            close = { exitPanelVisible = false },
            onDiscardPressed = { finish() }
        )
    }

    @Composable
    @Preview(
        showBackground = true,
        showSystemUi = true
    )
    private fun Test() {
        val context = LocalContext.current

        TruvideoSdkTheme {
            Content(
                viewModel = TruvideoSdkVideoViewModel(
                    context = context,
                    input = TruvideoSdkVideoFile.custom(""),
                    output = TruvideoSdkVideoFileDescriptor.custom(""),
                    previewMode = true
                )
            )
        }
    }
}