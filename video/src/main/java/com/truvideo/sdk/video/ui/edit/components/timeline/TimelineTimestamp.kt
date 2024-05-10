package com.truvideo.sdk.video.ui.edit.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truvideo.sdk.video.ui.edit.utils.TruvideoSdkVideoUtils
import truvideo.sdk.components.animated_opacity.TruvideoAnimatedOpacity

@Composable
fun TimelineTimestamp(
    visible: Boolean = false,
    startPosition: Float,
    start: Long,
    endPosition: Float,
    end: Long,
) {
    TruvideoAnimatedOpacity(if (visible) 1.0f else 0.0f) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .offset(x = startPosition.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = Color.White),
                contentAlignment = Alignment.Center

            ) {
                Text(
                    TruvideoSdkVideoUtils.timeToScreen(start),
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .offset(x = endPosition.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    TruvideoSdkVideoUtils.timeToScreen(end),
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
@Preview
private fun Test() {
    var visible by remember { mutableStateOf(true) }

    Box(modifier = Modifier.background(color = Color.Black)) {

        Column {
            TimelineTimestamp(
                visible = visible,
                start = 100,
                startPosition = 0f,
                end = 1000,
                endPosition = 40f,
            )
            Text(
                "Visible: $visible",
                modifier = Modifier.clickable {
                    visible = !visible
                },
                color = Color.White
            )
        }
    }

}