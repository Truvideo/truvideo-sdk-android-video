package com.truvideo.sdk.video.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.components.animated_collapse_visibility.TruvideoAnimatedCollapseVisibility
import com.truvideo.sdk.components.animated_rotation.TruvideoAnimatedRotation
import com.truvideo.sdk.components.icon_button.TruvideoIconButton
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation

@Composable
fun FileListItem(
    info: TruvideoSdkVideoInformation,
    onDeletePressed: () -> Unit = {},
    onEditPressed: () -> Unit = {},
) {
    var infoVisible by remember { mutableStateOf(false) }

    Card {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    info.path,
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f),
                    style = MaterialTheme.typography.bodySmall
                )
                TruvideoAnimatedRotation(if (infoVisible) 180f else 0f) {
                    TruvideoIconButton(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        color = Color.Transparent,
                        iconColor = Color.Black,
                        onPressed = { infoVisible = !infoVisible }
                    )
                }
                TruvideoIconButton(
                    imageVector = Icons.Outlined.Edit,
                    color = Color.Transparent,
                    iconColor = Color.Black,
                    onPressed = { onEditPressed() }
                )
                TruvideoIconButton(
                    imageVector = Icons.Outlined.Delete,
                    color = Color.Transparent,
                    iconColor = Color.Black,
                    onPressed = { onDeletePressed() }
                )
            }
        }

        TruvideoAnimatedCollapseVisibility(infoVisible) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Resolution: ${info.width}x${info.height}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Rotation: ${info.rotation}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

}

@Composable
@Preview(showBackground = true)
private fun Test() {
    FileListItem(
        info = TruvideoSdkVideoInformation(
            width = 100,
            height = 100,
            path = "path",
            rotation = 0,
            size = 0,
            audioCodec = "audio codec",
            videoCodec = "video codec",
            videoPixelFormat = "video pixel format",
            withVideo = true,
            withAudio = true,
            durationMillis = 0,
            audioSampleRate = 0
        )
    )
}