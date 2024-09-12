package com.truvideo.sdk.video.ui.edit.components.tab_bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truvideo.sdk.components.TruvideoColors
import com.truvideo.sdk.components.animated_value.animateColor
import com.truvideo.sdk.video.model.EditMode

@Composable
fun TabBar(
    mode: EditMode,
    onEditionModeChange: (mode: EditMode) -> Unit = {}
) {

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
            .background(TruvideoColors.gray)
            .fillMaxWidth()
            .height(70.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TabButton(
            title = "Trim",
            icon = Icons.Default.ContentCut,
            selected = mode == EditMode.Trimming,
            onPressed = { onEditionModeChange(EditMode.Trimming) }
        )

        TabButton(
            title = "Sound",
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            selected = mode == EditMode.Sound,
            onPressed = { onEditionModeChange(EditMode.Sound) }
        )

        TabButton(
            title = "Rotation",
            icon = Icons.AutoMirrored.Filled.RotateLeft,
            selected = mode == EditMode.Rotation,
            onPressed = { onEditionModeChange(EditMode.Rotation) }
        )
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

    val iconColorAnim = animateColor(value = calculateIconColor())

    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .clickable(
                onClick = { onPressed() },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White)
            )
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


