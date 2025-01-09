package com.truvideo.sdk.video.ui.activities.edit.components.tab_bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truvideo.sdk.components.TruvideoColors
import com.truvideo.sdk.components.animated_value.animateColor
import com.truvideo.sdk.components.animated_value.springAnimationColorSpec
import com.truvideo.sdk.video.ui.activities.edit.EditMode
import com.truvideo.sdk.video.ui.activities.edit.theme.TruvideoSdkTheme

@Composable
internal fun TabBar(
    mode: EditMode,
    onEditionModeChange: ((mode: EditMode) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
            .background(TruvideoColors.gray)
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(70.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        key("trim") {
            TabButton(
                title = "Trim",
                icon = Icons.Default.ContentCut,
                selected = mode == EditMode.Trimming,
                onPressed = { onEditionModeChange?.invoke(EditMode.Trimming) }
            )
        }

        key("sound") {
            TabButton(
                title = "Sound",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                selected = mode == EditMode.Sound,
                onPressed = { onEditionModeChange?.invoke(EditMode.Sound) }
            )
        }

        key("rotation") {
            TabButton(
                title = "Rotation",
                icon = Icons.AutoMirrored.Filled.RotateLeft,
                selected = mode == EditMode.Rotation,
                onPressed = { onEditionModeChange?.invoke(EditMode.Rotation) }
            )
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: ImageVector,
    selected: Boolean = false,
    onPressed: () -> Unit
) {
    fun calculateIconColor(): Color {
        if (selected) return Color.White
        return Color.White.copy(0.5f)
    }

    val iconColorAnim = animateColor(
        value = calculateIconColor(),
        spec = springAnimationColorSpec
    )

    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .clickable { onPressed() }
            .width(70.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(28.dp)) {
            Image(
                imageVector = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(iconColorAnim)
            )
        }

        Text(
            text = title,
            color = iconColorAnim,
            fontSize = 14.sp
        )
    }
}

@Composable
@Preview
private fun Test() {
    TruvideoSdkTheme {
        TabBar(mode = EditMode.Sound)
    }
}