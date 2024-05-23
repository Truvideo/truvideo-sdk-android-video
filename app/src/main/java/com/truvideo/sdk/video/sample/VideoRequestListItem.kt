package com.truvideo.sdk.video.sample

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truvideo.sdk.components.button.TruvideoButton
import com.truvideo.sdk.components.scale_button.TruvideoScaleButton
import com.truvideo.sdk.video.model.TruvideoSdkVideoConcatRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.CANCELED
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.COMPLETED
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.ERROR
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.IDLE
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.PROCESSING
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Composable
fun VideoRequestListItem(request: TruvideoSdkVideoRequest) {
    val context = LocalContext.current
    val buttonProcessEnabled = when (request.status) {
        IDLE -> true
        PROCESSING -> false
        ERROR -> true
        COMPLETED -> false
        CANCELED -> true
    }
    val buttonCancelEnabled = when (request.status) {
        IDLE -> false
        PROCESSING -> true
        ERROR -> false
        COMPLETED -> false
        CANCELED -> false
    }
    val isProcessing = request.status == PROCESSING

    val resultPath = when (request.type) {
        TruvideoSdkVideoRequestType.MERGE -> request.mergeData?.resultPath ?: ""
        TruvideoSdkVideoRequestType.CONCAT -> request.concatData?.resultPath ?: ""
        TruvideoSdkVideoRequestType.ENCODE -> request.encodeData?.resultPath ?: ""
    }

    val inputFiles: List<File> = when (request.type) {
        TruvideoSdkVideoRequestType.MERGE -> request.mergeData?.videoPaths?.map { File(it) } ?: listOf<File>()
        TruvideoSdkVideoRequestType.CONCAT -> request.concatData?.videoPaths?.map { File(it) } ?: listOf<File>()
        TruvideoSdkVideoRequestType.ENCODE -> {
            val path = request.encodeData?.videoPath ?: ""
            var file: File? = null
            if (path.trim().isNotEmpty()) file = File(path)
            if (file != null) {
                listOf(file)
            } else {
                listOf<File>()
            }
        }
    }


    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box() {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.Black.copy(0.1f))
                        .padding(8.dp)
                ) {
                    Row {
                        Text(request.type.name, modifier = Modifier.weight(1f))
                        Text(request.status.name)
                    }
                }

                Box(modifier = Modifier.padding(8.dp)) {
                    Column {
                        Text(request.id)
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize { _, _ -> }) {
                            if (request.errorMessage != null)
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color = Color.Red)
                                        .padding(4.dp)
                                ) {
                                    Column {
                                        Text(
                                            "Error",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${request.errorMessage}",
                                            color = Color.White,
                                        )

                                    }
                                }
                        }

                        if (inputFiles.isNotEmpty()) {
                            Box(modifier = Modifier.padding(top = 8.dp)) {
                                Column {
                                    inputFiles.forEach {
                                        key(it.path) {
                                            TruvideoScaleButton(
                                                onPressed = {
                                                    val intent = Intent(context, ViewerActivity::class.java)
                                                    intent.putExtra("filePath", it.path)
                                                    context.startActivity(intent)
                                                }
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(bottom = 2.dp)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(color = Color.Black.copy(0.1f))
                                                        .padding(4.dp)
                                                ) {
                                                    Text(
                                                        it.path,
                                                        fontSize = 10.sp,
                                                        lineHeight = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize { _, _ -> }
                        ) {
                            if (request.status == COMPLETED && resultPath.trim().isNotEmpty()) {
                                Box(modifier = Modifier.padding(top = 8.dp)) {
                                    Column {
                                        Text("Result")
                                        TruvideoScaleButton(
                                            onPressed = {
                                                val intent = Intent(context, ViewerActivity::class.java)
                                                intent.putExtra("filePath", resultPath)
                                                context.startActivity(intent)
                                            }
                                        ) {

                                            Box(
                                                modifier = Modifier
                                                    .padding(bottom = 2.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(color = Color.Black.copy(0.1f))
                                                    .padding(4.dp)
                                            ) {
                                                Text(
                                                    resultPath,
                                                    fontSize = 10.sp,
                                                    lineHeight = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        Box(modifier = Modifier.height(16.dp))

                        Row {
                            TruvideoButton(
                                enabled = buttonProcessEnabled,
                                fullWidth = false,
                                text = "Process",
                                onPressed = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            request.process()
                                        } catch (exception: Exception) {
                                            exception.printStackTrace()
                                        }
                                    }
                                }
                            )

                            Box(modifier = Modifier.width(4.dp))

                            TruvideoButton(
                                enabled = buttonCancelEnabled,
                                text = "Cancel",
                                fullWidth = false,
                                onPressed = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            request.cancel()
                                        } catch (exception: Exception) {
                                            exception.printStackTrace()
                                        }
                                    }
                                }
                            )

                            Box(modifier = Modifier.width(4.dp))

                            TruvideoButton(
                                enabled = !isProcessing,
                                text = "Delete",
                                fullWidth = false,
                                onPressed = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            request.delete()
                                        } catch (exception: Exception) {
                                            exception.printStackTrace()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun Test() {
    VideoRequestListItem(
        request = TruvideoSdkVideoRequest(
            id = "id",
            status = COMPLETED,
            createdAt = Date(),
            type = TruvideoSdkVideoRequestType.CONCAT,
            errorMessage = "test error message",
            concatData = TruvideoSdkVideoConcatRequestData(
                videoPaths = listOf("path1", "path2"),
                resultPath = "path"
            )
        )
    )
}