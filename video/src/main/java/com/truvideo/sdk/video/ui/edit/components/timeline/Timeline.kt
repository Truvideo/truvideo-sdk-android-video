package com.truvideo.sdk.video.ui.edit.components.timeline

import android.content.Context
import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.video.ui.edit.TimeLineFrame

@Composable
fun Timeline(
    enabled: Boolean = true,
    start: Long = 0,
    end: Long = 0,
    duration: Long = 0,
    position: Long = 0,
    positionVisible: Boolean = false,
    onStartChanged: ((position: Long, fromCenter: Boolean) -> Unit)? = null,
    onEndChanged: ((position: Long, fromCenter: Boolean) -> Unit)? = null,
    itemCount: Int,
    frames: List<TimeLineFrame>
) {
    val context = LocalContext.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(size, duration) {
        visible = size.width > 0L && duration > 0L
    }

    val buttonOffset = thumbWidth * 0.5f

    // Start
    var startDragging by remember { mutableStateOf(false) }
    var startPosition by remember { mutableFloatStateOf(0f) }
    var startDraggingPosition by remember { mutableFloatStateOf(0f) }
    var effectiveStartPosition by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(startDragging, startDraggingPosition, startPosition) {
        effectiveStartPosition = if (startDragging) startDraggingPosition else startPosition
    }

    LaunchedEffect(duration, start) {
        if (duration > 0L && size.width > 0) {
            val percentage = (start.toFloat() / duration).coerceIn(0.0f, 1.0f)
            val x = (context.pxToDp(size.width.toFloat()) * percentage) - buttonOffset
            x.coerceIn(-buttonOffset, context.pxToDp(size.width.toFloat()) - buttonOffset)
            startPosition = x
        }
    }

    // End
    var endDragging by remember { mutableStateOf(false) }
    var endPosition by remember { mutableFloatStateOf(0f) }
    var endDraggingPosition by remember { mutableFloatStateOf(0f) }
    var effectiveEndPosition by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(endDragging, endDraggingPosition, endPosition) {
        effectiveEndPosition = if (endDragging) endDraggingPosition else endPosition
    }

    LaunchedEffect(duration, end) {
        if (duration > 0L && size.width > 0) {
            val percentage = (end.toFloat() / duration).coerceIn(0.0f, 1.0f)
            val x = (context.pxToDp(size.width.toFloat()) * percentage) - buttonOffset
            x.coerceIn(-buttonOffset, context.pxToDp(size.width.toFloat()) - buttonOffset)
            endPosition = x
        }
    }

    // Position
    var pos by remember { mutableFloatStateOf(0f) }
    var posVisible by remember { mutableStateOf(false) }
    LaunchedEffect(position, duration, size, effectiveStartPosition, effectiveEndPosition) {
        if (size.width == 0 || duration == 0L) {
            posVisible = false
        } else {
            val percentage = (position.toFloat() / duration).coerceIn(0.0f, 1.0f)
            var x = (context.pxToDp(size.width.toFloat()) * percentage) - buttonOffset
            x = x.coerceIn(effectiveStartPosition, effectiveEndPosition)
            pos = x
            posVisible = true
        }
    }


    // Center
    var dragCenterDistance by remember { mutableFloatStateOf(0f) }
    var dragCenterTime by remember { mutableLongStateOf(0L) }
    var max by remember { mutableFloatStateOf(0f) }

    // Border
    fun calculateBorderColor(): Color {
        if (!enabled) return Color(0xFF616161)
        if (startDragging || endDragging) return Color(0xFFFFC107)
        return Color(0xFF616161)
    }

    val borderColor = remember { Animatable(calculateBorderColor()) }
    LaunchedEffect(startDragging, endDragging, enabled) { borderColor.animateTo(calculateBorderColor()) }

    fun calculateTimeForPosition(position: Float): Long {
        if (duration == 0L) return 0L
        if (size.width == 0) return 0L


        val percentage = (position / size.width).coerceIn(0.0f, 1.0f)
        return (percentage * duration).toLong()
    }

    fun onDragStartThumb(delta: Float, max: Float? = null, fromCenter: Boolean = false): Long {
        startDraggingPosition += delta
        startDraggingPosition = startDraggingPosition.coerceIn(-buttonOffset, context.pxToDp(size.width.toFloat()) - buttonOffset)
        startDraggingPosition = if (max != null) {
            startDraggingPosition.coerceAtMost(max)
        } else {
            startDraggingPosition.coerceAtMost(endPosition - thumbWidth)
        }

        val p = context.dpToPx(startDraggingPosition + buttonOffset)
        val time = calculateTimeForPosition(p)
        onStartChanged?.invoke(time, fromCenter)

        return time
    }


    fun onDragEndThumb(delta: Float, max: Float? = null, fromCenter: Boolean = false): Long {
        endDraggingPosition += delta
        endDraggingPosition = endDraggingPosition.coerceIn(-buttonOffset, context.pxToDp(size.width.toFloat()) - buttonOffset)
        endDraggingPosition = if (max != null) {
            endDraggingPosition.coerceAtLeast(max)
        } else {
            endDraggingPosition.coerceAtLeast(startPosition + thumbWidth)
        }


        val p = context.dpToPx(endDraggingPosition + buttonOffset)
        val time = calculateTimeForPosition(p)
        onEndChanged?.invoke(time, fromCenter)

        return time
    }


    fun onDragCenter(delta: Float) {
        max = context.pxToDp(size.width.toFloat()) - dragCenterDistance - buttonOffset
        onDragStartThumb(delta, max = max, fromCenter = true)

        // End
        endDraggingPosition = startDraggingPosition + dragCenterDistance
        val p = context.dpToPx(endDraggingPosition + buttonOffset)
        val time = calculateTimeForPosition(p)
        onEndChanged?.invoke(time, true)
    }

    Box(
        modifier = Modifier.height(60.dp)
    ) {
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color = Color.Black)
        ) {
            // Preview
            TimeLinePreview(
                itemCount = itemCount,
                frames = frames,
            )

            if (visible) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Start Dim
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width((effectiveStartPosition + buttonOffset + 16).dp)
                            .background(color = Color.Black.copy(0.4f))
                    )

                    // End Dim
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(((context.pxToDp(size.width.toFloat()) - effectiveEndPosition - buttonOffset + 16)).dp)
                            .offset(x = (effectiveEndPosition + buttonOffset).dp)
                            .background(color = Color.Black.copy(0.4f))
                    )

                    // Center
                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .width((effectiveEndPosition - effectiveStartPosition).dp)
                        .offset((effectiveStartPosition + thumbWidth).dp)
                        .background(color = Color.Transparent)
                        .pointerInput(enabled) {
                            if (enabled) {
                                detectDragGestures(
                                    onDragStart = {
                                        dragCenterDistance = endPosition - startPosition
                                        dragCenterTime = end - start
                                        startDraggingPosition = startPosition
                                        endDraggingPosition = endPosition
                                        startDragging = true
                                        endDragging = true
                                    },
                                    onDragCancel = {
                                        startDragging = false
                                        endDragging = false
                                    },
                                    onDragEnd = {
                                        startDragging = false
                                        endDragging = false
                                    }) { change, dragAmount ->
                                    change.consume()
                                    val x = context.pxToDp(dragAmount.x)
                                    onDragCenter(x)
                                }
                            }
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = buttonOffset.dp)
            .onSizeChanged { size = it }
        ) {


            // Position
            TimelinePosition(
                visible = visible && positionVisible, position = pos
            )

            // Border
            if (visible) {
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .width((effectiveEndPosition - effectiveStartPosition).dp)
                        .offset(x = (effectiveStartPosition + buttonOffset).dp)
                        .border(width = 4.dp, color = borderColor.value)
                )
            }
            if (visible) {
                Box(modifier = Modifier.offset(x = effectiveStartPosition.dp)) {
                    TimelineThumb(
                        isLeft = true,
                        enabled = enabled && duration > 0L && size.width > 0L,
                        selected = startDragging || endDragging,
                        onDragStart = {
                            startDraggingPosition = startPosition
                            startDragging = true
                        },
                        onDragEnd = { startDragging = false },
                        onDrag = { onDragStartThumb(it) }
                    )
                }
            }

            // End
            if (visible) {
                Box(modifier = Modifier.offset(x = effectiveEndPosition.dp)) {
                    TimelineThumb(isLeft = false,
                        enabled = enabled && duration > 0L && size.width > 0L,
                        selected = startDragging || endDragging,
                        onDragStart = {
                            endDraggingPosition = endPosition
                            endDragging = true
                        },
                        onDragEnd = { endDragging = false },
                        onDrag = { onDragEndThumb(it) })
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Test() {
    var duration by remember { mutableLongStateOf(1000L) }
    var position by remember { mutableLongStateOf(600L) }
    var positionVisible by remember { mutableStateOf(true) }
    var start by remember { mutableLongStateOf(250L) }
    var end by remember { mutableLongStateOf(750L) }
    var enabled by remember { mutableStateOf(true) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Red)
    ) {

        Column {

            Timeline(
                enabled = enabled,
                start = start,
                end = end,
                duration = duration,
                position = position,
                positionVisible = positionVisible,
                onStartChanged = { pos, _ -> start = pos },
                onEndChanged = { pos, _ -> end = pos },
                itemCount = 10,
                frames = listOf()
            )
            Text(
                "$start - $end from $duration", color = Color.White
            )
            Text(
                "Duration: $duration", modifier = Modifier.clickable {
                    duration = if (duration == 0L) {
                        1000L
                    } else {
                        0L
                    }
                }, color = Color.White
            )
            Text(
                "Position: $position", modifier = Modifier.clickable {
                    position = if (position == 0L) {
                        500L
                    } else {
                        0L
                    }
                }, color = Color.White
            )
            Text(
                "Position Visible: $positionVisible", modifier = Modifier.clickable {
                    positionVisible = !positionVisible
                }, color = Color.White
            )
            Text(
                "Enabled: $enabled", modifier = Modifier.clickable {
                    enabled = !enabled
                }, color = Color.White
            )
        }
    }

}

fun Context.dpToPx(dp: Float): Float {
    val density = resources.displayMetrics.density
    return dp * density
}
