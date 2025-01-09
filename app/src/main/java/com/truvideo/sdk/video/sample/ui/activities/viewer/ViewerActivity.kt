package com.truvideo.sdk.video.sample.ui.activities.viewer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import java.io.File


class ViewerActivity : ComponentActivity() {

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filePath = intent.getStringExtra("filePath") ?: ""
        enableEdgeToEdge()
        setContent {
            TruVideoSdkVideoModuleTheme {
                Content(
                    filePath = filePath
                )
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
    @Composable
    private fun Content(filePath: String) {
        val context = LocalContext.current
        val isVideo = filePath.endsWith("mp4") || filePath.endsWith("webm")

        var exoPlayer: ExoPlayer? = null
        if (isVideo) {
            exoPlayer = remember(context) { ExoPlayer.Builder(context).build() }
            LaunchedEffect(filePath) {
                val mediaItem = MediaItem.fromUri(Uri.fromFile(File(filePath)))
                exoPlayer.apply {
                    setMediaItem(mediaItem)
                    prepare()
                    play()
                }
            }
        }

        DisposableEffect(Unit) { onDispose { exoPlayer?.release() } }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {Text("")},
                    navigationIcon = {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                    }
                )
            }
        ){ innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isVideo) {
                    AndroidView(
                        factory = {
                            PlayerView(it).apply {
                                player = exoPlayer
                            }
                        },
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(0.dp))
                            .fillMaxSize(),
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GlideImage(
                            model = Uri.fromFile(File(filePath)),
                            contentDescription = ""
                        )
                    }
                }
            }
        }
    }

    @Composable
    @Preview(
        showBackground = true,
        showSystemUi = true
    )
    private fun Test() {
        TruVideoSdkVideoModuleTheme {
            Content(
                filePath = ""
            )
        }
    }

}