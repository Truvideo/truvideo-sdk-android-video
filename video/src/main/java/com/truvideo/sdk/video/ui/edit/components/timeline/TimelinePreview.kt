package com.truvideo.sdk.video.ui.edit.components.timeline

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.video.ui.edit.TimeLineFrame

@Composable
internal fun TimeLinePreview(
    modifier: Modifier = Modifier,
    itemCount: Int,
    frames: List<TimeLineFrame>,
) {
    val context = LocalContext.current
    var size by remember { mutableStateOf(IntSize.Zero) }

    var previewWidth by remember { mutableFloatStateOf(0f) }
    var previewHeight by remember { mutableFloatStateOf(0f) }
    var imageSize by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(size, itemCount) {
        if (size.width > 0) {
            previewWidth = context.pxToDp(size.width.toFloat()) / itemCount
        }

        if (size.height > 0) {
            previewHeight = size.height.toFloat()
        }

        imageSize = Math.max(previewWidth, previewHeight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .clip(RoundedCornerShape(0.dp))
            .onSizeChanged { size = it }
    ) {
        LazyRow(
            userScrollEnabled = false,
            modifier = modifier
        ) {
            items(
                frames,
                key = { item -> item.index }
            ) {
                Box(
                    modifier = Modifier
                        .width(previewWidth.dp)
                        .fillMaxHeight()
                        .background(color = Color.Black)
                        .clip(RoundedCornerShape(0.dp))
                ) {

                    if (it.bitmap != null) {
                        Image(
                            it.bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    }

                }
            }
        }
    }
}

@Preview
@Composable
private fun Test() {
    Column {
        TimeLinePreview(
            itemCount = 10,
            frames = listOf()
        )
    }
}


fun Context.pxToDp(px: Float): Float {
    val density = resources.displayMetrics.density
    return px / density
}