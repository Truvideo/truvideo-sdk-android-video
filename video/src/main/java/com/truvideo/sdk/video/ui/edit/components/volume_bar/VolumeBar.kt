package com.truvideo.sdk.video.ui.edit.components.volume_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import truvideo.sdk.components.TruvideoColors
import truvideo.sdk.components.icon_button.TruvideoIconButton
import truvideo.sdk.components.vertical_slider.TruvideoVerticalSlider

@Composable
fun VolumeBar(
    value: Float,
    onChanged: (value: Float) -> Unit
) {
    val isMuted = value == 0f

    Column {
        TruvideoVerticalSlider(
            value = value,
            trackColor = Color.White,
            backgroundColor = TruvideoColors.gray,
            onChange = { onChanged(it) },
            modifier = Modifier
                .width(40.dp)
                .height(150.dp)
                .shadow(20.dp, shape = CircleShape)
                .clip(CircleShape)
        )

        Box(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .shadow(20.dp, CircleShape)
                .background(TruvideoColors.gray, CircleShape)
        ) {
            TruvideoIconButton(
                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                selected = isMuted,
                onPressed = {
                    if (isMuted) {
                        onChanged(1f)
                    } else {
                        onChanged(0f)
                    }
                }
            )
        }
    }
}

@Composable
@Preview
private fun Test() {
    var value by remember { mutableFloatStateOf(0.0f) }
    VolumeBar(
        value = value,
        onChanged = { value = it }
    )
}