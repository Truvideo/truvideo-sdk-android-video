package com.truvideo.sdk.video.ui.edit.components.play_button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.truvideo.sdk.components.animated_fade_visibility.TruvideoAnimatedFadeVisibility
import com.truvideo.sdk.components.button.TruvideoIconButton

@Composable
fun PlayButton(
    isPlaying: Boolean = false,
    onPressed: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Playing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (onPressed != null) {
                                onPressed()
                            }
                        }
                    )
                }
        )


        // Not playing
        Box(modifier = Modifier.align(Alignment.Center)) {
            TruvideoAnimatedFadeVisibility(!isPlaying) {
                TruvideoIconButton(
                    icon = Icons.Default.PlayArrow,
                    onPressed = {
                        if (onPressed != null) {
                            onPressed()
                        }
                    }
                )
            }
        }
    }
}

@Composable
@Preview
private fun Test() {
    var isPlaying by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PlayButton(
                    isPlaying = isPlaying,
                    onPressed = {
                        isPlaying = !isPlaying
                    }
                )
            }
            Text(
                "Playing: $isPlaying",
                modifier = Modifier.clickable {
                    isPlaying = !isPlaying
                }
            )
        }
    }

}