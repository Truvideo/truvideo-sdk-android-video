package com.truvideo.sdk.video.ui.activities.edit.components.timeline

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.components.TruvideoColors
import com.truvideo.sdk.components.animated_fade_visibility.TruvideoAnimatedFadeVisibility
import com.truvideo.sdk.components.animated_value.animateColor
import com.truvideo.sdk.components.animated_value.animateFloat
import com.truvideo.sdk.components.animated_value.springAnimationFloatSpec
import com.truvideo.sdk.video.ui.utils.ContextUtils.dpToPx
import com.truvideo.sdk.video.ui.utils.ContextUtils.pxToDp
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun VideoThumbnailPreview(
    enabled: Boolean = true,
    start: Float = 0f,
    end: Float = 0f,
    duration: Float = 0f,
    position: Float = 0f,
    positionVisible: Boolean = false,
    updateStart: ((position: Float, fromCenter: Boolean) -> Unit)? = null,
    updateEnd: ((position: Float, fromCenter: Boolean) -> Unit)? = null,
    itemCount: Int,
    itemBuilder: @Composable (index: Int) -> Unit = {}
) {
    val context = LocalContext.current

    val indicatorW = 16f
    val indicatorPadding = 16f
    val iw = indicatorW + (indicatorPadding * 2)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = indicatorPadding.dp)
    ) {

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val w = context.dpToPx(maxWidth.value)
            var dragging by remember { mutableStateOf(false) }
            val indicatorColor = if (dragging) TruvideoColors.amber else Color.White
            val indicatorColorAnim = animateColor(value = indicatorColor)

            fun calculateStartXFromValue(value: Float, duration: Float, w: Float): Float {
                if (duration == 0f) return 0f
                return (value / duration).coerceIn(0f, 1f) * w
            }

            fun calculateEndXFromValue(value: Float, duration: Float, w: Float): Float {
                if (duration == 0f) return w
                return (value / duration).coerceIn(0f, 1f) * w
            }

            var startX by remember { mutableFloatStateOf(calculateStartXFromValue(start, duration, w)) }
            val startXAnim = animateFloat(
                value = startX,
                spec = springAnimationFloatSpec,
                animate = !dragging
            )

            var endX by remember { mutableFloatStateOf(calculateEndXFromValue(end, duration, w)) }
            val endXAnim = animateFloat(
                value = endX,
                spec = springAnimationFloatSpec,
                animate = !dragging
            )


            LaunchedEffect(start, duration, w) {
                if (dragging) return@LaunchedEffect
                startX = calculateStartXFromValue(start, duration, w)
            }

            LaunchedEffect(end, duration, w) {
                if (dragging) return@LaunchedEffect
                endX = calculateEndXFromValue(end, duration, w)
            }



            suspend fun PointerInputScope.onTapDown() {
                detectTapGestures(
                    onPress = {
                        dragging = true
                        val pressed = try {
                            awaitRelease()
                            true
                        } catch (_: Exception) {
                            false
                        }

                        if (pressed) dragging = false
                    }
                )
            }

            suspend fun PointerInputScope.onDrag(callback: (dragAmount: Offset) -> Unit) {
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false }
                ) { change, dragAmount ->
                    change.consume()
                    callback(dragAmount)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                        .background(TruvideoColors.gray)
                ) {
                    Row(Modifier.fillMaxSize()) {
                        for (i in 0 until itemCount) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                itemBuilder(i)
                            }
                        }
                    }

                }

                // Start shadow
                Box(
                    modifier = Modifier
                        .width(context.pxToDp(startXAnim + indicatorW * 0.5f).dp)
                        .fillMaxHeight()
                        .background(Color.Black.copy(0.5f))

                )

                // End shadow
                Box(
                    modifier = Modifier
                        .offset(context.pxToDp(endXAnim - indicatorW * 0.5f).dp)
                        .width(context.pxToDp(w - endXAnim + indicatorW * 0.5f).dp)
                        .fillMaxHeight()
                        .background(Color.Black.copy(0.5f))

                )

                // Center
                Box(
                    modifier = Modifier
                        .offset(
                            x = context.pxToDp(startXAnim).dp
                        )
                        .width(
                            context.pxToDp(endXAnim - startXAnim).dp
                        )
                        .fillMaxHeight()

                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = (indicatorPadding + indicatorW).dp)
                            .fillMaxSize()
                            .pointerInput(w, duration, enabled) {
                                if (!enabled) return@pointerInput
                                onTapDown()
                            }
                            .pointerInput(w, duration, enabled) {
                                if (!enabled) return@pointerInput
                                onDrag { dragAmount ->
                                    val currentW = endX - startX

                                    startX += dragAmount.x
                                    startX = startX.coerceIn(0f, w - currentW)
                                    endX = startX + currentW

                                    val newStartValue = (startX / w) * duration
                                    updateStart?.invoke(newStartValue, true)

                                    val newEndValue = (endX / w) * duration
                                    updateEnd?.invoke(newEndValue, true)
                                }
                            }
                    )
                }

                // Position
                val positionX = if (duration == 0f) {
                    0.5f * w
                } else {
                    (position / duration).coerceIn(0f, 1f) * w
                }.coerceIn(
                    startX + context.dpToPx(indicatorW * 0.5f),
                    endXAnim - context.dpToPx(indicatorW * 0.5f)
                )

                Box(
                    modifier = Modifier
                        .offset(x = context.pxToDp(positionX).dp - 2.dp)
                        .width(4.dp)
                        .fillMaxHeight()
                ) {
                    TruvideoAnimatedFadeVisibility(positionVisible && duration > 0f && !dragging) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                        )
                    }
                }


                // Start indicator
                Box(
                    modifier = Modifier
                        .offset(
                            x = context.pxToDp(startXAnim).dp - indicatorPadding.dp
                        )
                        .fillMaxHeight()
                        .width(iw.dp)
                        .padding(horizontal = indicatorPadding.dp)
                        .pointerInput(w, duration, enabled) {
                            if (!enabled) return@pointerInput
                            onTapDown()
                        }
                        .pointerInput(w, duration, enabled) {
                            if (!enabled) return@pointerInput
                            onDrag { dragAmount ->
                                startX += dragAmount.x

                                val maxX = endX - context.dpToPx(iw + indicatorPadding)
                                startX = startX.coerceIn(0f, maxX)

                                val newStartValue = (startX / w) * duration
                                Log.d("TruvideoSdkVideo", "Update start $newStartValue. duration: $duration")
                                updateStart?.invoke(newStartValue, false)
                            }
                        }
                ) {
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    bottomStart = 4.dp
                                )
                            )
                            .background(indicatorColorAnim)
                            .fillMaxSize()
                    ){
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // End indicator
                Box(
                    modifier = Modifier
                        .offset(
                            x = context.pxToDp(endXAnim).dp - indicatorPadding.dp - indicatorW.dp
                        )
                        .fillMaxHeight()
                        .width(iw.dp)
                        .padding(horizontal = indicatorPadding.dp)
                        .pointerInput(w, duration, enabled) {
                            if (!enabled) return@pointerInput
                            onTapDown()
                        }
                        .pointerInput(w, duration, enabled) {
                            if (!enabled) return@pointerInput
                            onDrag { dragAmount ->
                                endX += dragAmount.x

                                val minX = startX + context.dpToPx(iw + indicatorPadding)
                                endX = endX.coerceIn(minX, w)

                                val newEndValue = (endX / w) * duration
                                Log.d("TruvideoSdkVideo", "Update end $newEndValue. duration: $duration")
                                updateEnd?.invoke(newEndValue, false)
                            }
                        }
                ) {
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    topEnd = 4.dp,
                                    bottomEnd = 4.dp
                                )
                            )
                            .background(indicatorColorAnim)
                            .fillMaxSize()
                    ){
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }


                // Top line
                Box(
                    modifier = Modifier
                        .offset(context.pxToDp(startXAnim).dp + (indicatorW * 0.5f).dp)
                        .height(4.dp)
                        .width(context.pxToDp(endXAnim - startXAnim).dp - indicatorW.dp)
                        .background(indicatorColorAnim)
                )

                // Bottom line
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(context.pxToDp(startXAnim + (indicatorW * 0.5f)).dp)
                        .height(4.dp)
                        .width(context.pxToDp(endXAnim - startXAnim - (indicatorW)).dp)
                        .background(indicatorColorAnim)
                )


                // Top shadow
                Box(
                    modifier = Modifier
                        .offset(
                            x = context.pxToDp(startXAnim).dp + indicatorW.dp,
                            y = 4.dp
                        )
                        .height(8.dp)
                        .width(context.pxToDp(endXAnim - startXAnim).dp - indicatorW.dp - indicatorPadding.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = persistentListOf(
                                    Color.Black.copy(0.5f),
                                    Color.Black.copy(0.0f)
                                )
                            )
                        )
                )


                // Bottom shadow
                Box(
                    modifier = Modifier
                        .offset(
                            x = context.pxToDp(startXAnim).dp + indicatorW.dp,
                            y = (48 - 8 - 4).dp
                        )
                        .height(8.dp)
                        .width(context.pxToDp(endXAnim - startXAnim).dp - indicatorW.dp - indicatorPadding.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = persistentListOf(
                                    Color.Black.copy(0.0f),
                                    Color.Black.copy(0.5f)
                                )
                            )
                        )
                )


                // Left shadow
                Box(
                    modifier = Modifier
                        .offset(
                            x = context.pxToDp(startXAnim).dp + indicatorW.dp,
                            y = 4.dp
                        )
                        .height(40.dp)
                        .width(8.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = persistentListOf(
                                    Color.Black.copy(0.5f),
                                    Color.Black.copy(0.0f)
                                )
                            )
                        )
                )

                // Right shadow
                Box(
                    modifier = Modifier
                        .offset(
                            x = context.pxToDp(endXAnim).dp - indicatorW.dp - 8.dp,
                            y = 4.dp
                        )
                        .height(40.dp)
                        .width(8.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = persistentListOf(
                                    Color.Black.copy(0.0f),
                                    Color.Black.copy(0.5f)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Test() {
    var start by remember { mutableFloatStateOf(100f) }
    var end by remember { mutableFloatStateOf(900f) }
    var position by remember { mutableFloatStateOf(900f) }
    var positionVisible by remember { mutableStateOf(true) }

    Column {
        Box(Modifier.background(Color.Black)) {
            VideoThumbnailPreview(
                duration = 1000f,
                start = start,
                updateStart = { value, _ -> start = value },
                end = end,
                updateEnd = { value, _ -> end = value },
                position = position,
                positionVisible = positionVisible,
                itemCount = 5
            ) { index ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(index.toString(), color = Color.White)
                }
            }
        }

        Text("Start: $start")
        Text("End: $end")
        Text("Position: $positionVisible", Modifier.clickable {
            positionVisible = !positionVisible
        })

        Text(
            "Reset",
            modifier = Modifier.clickable {
                start = 0f
                end = 1000f
                position = 500f
            }
        )
    }
}