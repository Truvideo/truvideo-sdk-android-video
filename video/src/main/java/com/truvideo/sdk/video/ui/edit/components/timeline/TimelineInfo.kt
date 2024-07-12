package com.truvideo.sdk.video.ui.edit.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truvideo.sdk.video.ui.edit.utils.TruvideoSdkVideoUtils

@Composable
fun TimelineInfo(
    start: Long,
    end: Long,
    duration: Long,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color = Color(0xFF616161))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            "${TruvideoSdkVideoUtils.timeToScreen(start)} - ${TruvideoSdkVideoUtils.timeToScreen(end)}",
            color = Color.White,
            fontSize = 12.sp
        )
    }

}

@Composable
@Preview
private fun Test() {

    Box(modifier = Modifier.background(color = Color.Black)) {

        Column {
            TimelineInfo(
                start = 100,
                end = 1000,
                duration = 1000
            )
        }
    }

}