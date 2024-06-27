package com.truvideo.sdk.video.ui.edit.components.tab_bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.truvideo.sdk.components.scale_button.TruvideoScaleButton
import com.truvideo.sdk.video.model.EditMode
import androidx.compose.animation.Animatable as AnimateColor

@Composable
fun TabBar(
    mode: EditMode,
    onEditionModeChange: ((mode: EditMode) -> Unit)? = null
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
            onPressed = { onEditionModeChange?.invoke(EditMode.Trimming) }
        )

        TabButton(
            title = "Sound",
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            selected = mode == EditMode.Sound,
            onPressed = { onEditionModeChange?.invoke(EditMode.Sound) }
        )

        TabButton(
            title = "Rotation",
            icon = Icons.AutoMirrored.Filled.RotateLeft,
            selected = mode == EditMode.Rotation,
            onPressed = { onEditionModeChange?.invoke(EditMode.Rotation) }
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

    val effectiveIconColor = remember { AnimateColor(calculateIconColor()) }
    LaunchedEffect(selected) { effectiveIconColor.animateTo(calculateIconColor()) }


    TruvideoScaleButton(
        onPressed = onPressed
    ) {
        Column(
            modifier = Modifier.width(70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(modifier = Modifier.size(28.dp)) {
                Image(
                    imageVector = icon,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(effectiveIconColor.value)
                )
            }

            Text(
                text = title,
                color = effectiveIconColor.value,
                fontSize = 14.sp
            )
        }
    }

}

@Composable
@Preview
private fun Test() {

}