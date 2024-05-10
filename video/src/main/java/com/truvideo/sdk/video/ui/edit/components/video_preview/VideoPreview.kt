package com.truvideo.sdk.video.ui.edit.components.video_preview

import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import truvideo.sdk.components.animated_fit.TruvideoAnimatedFit
import truvideo.sdk.components.animated_scale.TruvideoAnimatedScale

@Composable
fun VideoPreview(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer? = null,
    rotation: Float = 0f,
    fullScreen: Boolean = false,
) {
    val view = LocalView.current
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var isPlaying by remember { mutableStateOf(false) }

    fun calculateAspectRatio(): Float {
        val width = exoPlayer?.videoSize?.width ?: 0
        val height = exoPlayer?.videoSize?.height ?: 0
        if (width == 0 || height == 0) return 1f
        return width.toFloat() / height
    }

    val listener = remember {
        object : Player.Listener {
            override fun onIsPlayingChanged(value: Boolean) {
                super.onIsPlayingChanged(value)
                isPlaying = value
            }

            override fun onSurfaceSizeChanged(width: Int, height: Int) {
                super.onSurfaceSizeChanged(width, height)
                aspectRatio = calculateAspectRatio()
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                aspectRatio = calculateAspectRatio()
            }
        }
    }

    LaunchedEffect(Unit) {
        aspectRatio = calculateAspectRatio()
    }

    DisposableEffect(exoPlayer) {
        exoPlayer?.removeListener(listener)
        exoPlayer?.addListener(listener)
        onDispose { exoPlayer?.removeListener(listener) }
    }

    AspectRatioComponent(
        aspectRatio = aspectRatio,
        fullScreen = fullScreen,
        rotation = rotation,
    ) {

        if (!view.isInEditMode) {
            VideoSurface(
                player = exoPlayer!!,
                modifier = modifier
                    .clip(shape = RoundedCornerShape(12.dp))
                    .fillMaxSize(),
            )
        }
    }
}

@Composable
private fun AspectRatioComponent(
    aspectRatio: Float,
    fullScreen: Boolean = false,
    rotation: Float,
    content: @Composable() (() -> Unit)? = null
) {
    var size by remember { mutableStateOf(Size.Zero) }
    var scale by remember { mutableFloatStateOf(1.0f) }

    LaunchedEffect(fullScreen) {
        scale = if (fullScreen) {
            if (size.width == 0f || size.height == 0f) {
                1f
            } else {
                val turns = rotation % 360
                val contentAspectRatio: Float
                if (turns == 0f || turns == 180f) {
                    contentAspectRatio = size.width / size.height
                } else {
                    contentAspectRatio = size.height / size.width
                }

                if (contentAspectRatio > aspectRatio) {
                    contentAspectRatio / aspectRatio
                } else {
                    aspectRatio / contentAspectRatio
                }
            }
        } else {
            1f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
            .onSizeChanged { size = Size(it.width.toFloat(), it.height.toFloat()) }
    ) {

        TruvideoAnimatedScale(scale = scale) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                TruvideoAnimatedFit(
                    aspectRatio = aspectRatio,
                    rotation = rotation,
                    modifier = Modifier.fillMaxSize(),
                    contentModifier = Modifier.align(Alignment.Center)
                ) {
                    if (content != null) content()
                }
            }
        }
    }
}

@Composable
private fun VideoSurface(
    player: Player,
    modifier: Modifier
) {
    val context = LocalContext.current

    val videoView = remember {
        val view = TextureView(context)
        view.tag = player
        player.setVideoTextureView(view)
        view
    }

    AndroidView(
        factory = { videoView },
        modifier = modifier,
    )
    DisposableEffect(Unit) {
        onDispose {
            player.clearVideoTextureView(videoView)
        }
    }

}

@Composable
@Preview
private fun Test() {
    var fullScreen by remember { mutableStateOf(true) }
    var rotation by remember { mutableStateOf(90f) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                AspectRatioComponent(
                    fullScreen = fullScreen,
                    aspectRatio = 2 / 16f,
                    rotation = rotation
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = Color.Red)
                            .fillMaxSize()
                    ) {
                        Text("Hola")
                        Text("Hola", modifier = Modifier.align(Alignment.TopCenter))
                    }
                }
            }

            Text("Fullscreen: $fullScreen",
                modifier = Modifier.clickable {
                    fullScreen = !fullScreen
                }
            )

            Text("Rotation: $rotation",
                modifier = Modifier.clickable {
                    rotation += 90f
                }
            )

            Text("Reset rotation",
                modifier = Modifier.clickable {
                    rotation = 0f
                }
            )
        }

    }
}