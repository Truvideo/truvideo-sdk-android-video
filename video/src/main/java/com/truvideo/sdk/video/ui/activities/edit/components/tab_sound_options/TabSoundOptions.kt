package com.truvideo.sdk.video.ui.activities.edit.components.tab_sound_options

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.components.animated_content.TruvideoAnimatedContent
import com.truvideo.sdk.video.ui.activities.edit.theme.TruvideoSdkTheme

@Composable
fun TabSoundOptions(
    useNoiseCanceledVideo: Boolean = false,
    isProcessingClearNoise: Boolean = false,
    onUseNoiseCanceledVideoPressed: () -> Unit = {}
) {
    Box(
        Modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = !isProcessingClearNoise,
            ) {
                onUseNoiseCanceledVideoPressed()
            }
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        TruvideoAnimatedContent(targetState = isProcessingClearNoise) { isProcessingTarget ->
            if (isProcessingTarget) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Box(Modifier.width(8.dp))
                    Text(
                        "Processing",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            } else {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TruvideoAnimatedContent(targetState = useNoiseCanceledVideo) { useNoiseCanceledVideoTarget ->
                        Icon(
                            imageVector = if (useNoiseCanceledVideoTarget) Icons.Outlined.Check else Icons.Outlined.Circle,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(Modifier.width(8.dp))
                    Text(
                        "Noise cancelling",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Test() {
    TruvideoSdkTheme {
        Box(modifier = Modifier.background(Color.Black)) {
            TabSoundOptions(
                isProcessingClearNoise = false
            )
        }
    }
}