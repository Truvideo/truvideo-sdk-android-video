package com.truvideo.sdk.video.ui.edit.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.truvideo.sdk.components.animated_opacity.TruvideoAnimatedOpacity

@Composable
fun TimelinePosition(
    visible: Boolean = true,
    position: Float,
) {
    Box(
        modifier = Modifier
            .height(60.dp)
            .width(40.dp)
            .offset(x = position.dp),
        contentAlignment = Alignment.Center,
    ) {
        TruvideoAnimatedOpacity(opacity = if (visible) 1.0f else 0.0f) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = Color.White)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    var visible by remember { mutableStateOf(true) }

    Box(modifier = Modifier.background(color = Color.Black)) {
        Column {
            TimelinePosition(
                visible = visible,
                position = 10f,
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
