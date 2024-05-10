package com.truvideo.sdk.video.ui.edit.components.timeline

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Animatable as AnimateColor

var thumbWidth = 48f
@Composable
fun TimelineThumb(
    isLeft: Boolean = false,
    enabled: Boolean = true,
    selected: Boolean = false,
    onDrag: ((delta: Float) -> Unit)? = null,
    onDragStart: (() -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null
) {
    val context: Context = LocalContext.current

    var dragging by remember { mutableStateOf(false) }


    fun calculateColor(): Color {
        if (!enabled) return Color(0xFF616161)
        if (selected) return Color(0xFFFFC107)
        return Color(0xFF616161)
    }

    fun calculateIconColor(): Color {
        if (!enabled) return Color.White
        if (selected) return Color.Black
        return Color.White
    }

    val backgroundColor = remember { AnimateColor(calculateColor()) }
    LaunchedEffect(selected, enabled) { backgroundColor.animateTo(calculateColor()) }

    val iconColor = remember { AnimateColor(calculateIconColor()) }
    LaunchedEffect(selected, enabled) { iconColor.animateTo(calculateIconColor()) }

    var modifier = Modifier
        .width(thumbWidth.dp)
        .height(60.dp)
        .background(color = Color.Transparent)

    val shape = if (isLeft) {
        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
    } else {
        RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
    }

    if (enabled) {
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragging = true
                        onDragStart?.invoke()
                    },
                    onDragCancel = {
                        dragging = false
                        onDragEnd?.invoke()
                    },
                    onDragEnd = {
                        dragging = false
                        onDragEnd?.invoke()
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val x = context.pxToDp(dragAmount.x)
                    onDrag?.invoke(x)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        dragging = false
                        onDragStart?.invoke()
                        tryAwaitRelease()
                        if (!dragging) {
                            onDragEnd?.invoke()
                        }
                    },
                )
            }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(60.dp)
                .clip(shape)
                .background(backgroundColor.value),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                if (isLeft) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = iconColor.value
            )
        }

    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    var isLeft by remember { mutableStateOf(false) }
    var isEnabled by remember { mutableStateOf(true) }
    var offset by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(false) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color = Color.Red)
                .offset(x = offset.dp)
        ) {
            TimelineThumb(
                isLeft = isLeft,
                enabled = isEnabled,
                selected = selected,
                onDragStart = { dragging = true },
                onDragEnd = { dragging = false },
                onDrag = { offset += it }
            )
        }

        Text("Left: $isLeft",
            modifier = Modifier.clickable {
                isLeft = !isLeft
            }
        )

        Text("Enabled: $isEnabled",
            modifier = Modifier.clickable {
                isEnabled = !isEnabled
            }
        )

        Text("Selected: $selected",
            modifier = Modifier.clickable {
                selected = !selected
            }
        )

        Text("Dragging: $dragging")
    }
}
