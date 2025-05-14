package com.truvideo.sdk.video.sample.ui.activities.home.components

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.video.model.TruvideoSdkVideoFrameRate
import com.truvideo.sdk.video.model.TruvideoSdkVideoMergeRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.CANCELED
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.COMPLETED
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.ERROR
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.IDLE
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus.PROCESSING
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import com.truvideo.sdk.video.sample.ui.activities.viewer.ViewerActivity
import com.truvideo.sdk.video.sample.ui.dialogs.InfoDialog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VideoRequestListItem(
    request: TruvideoSdkVideoRequest,
    addToFiles: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
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
    val buttonDeleteEnabled = request.status != PROCESSING
    val buttonAddToFilesEnabled = request.status == COMPLETED

    val resultPath = remember(request) {
        when (request.type) {
            TruvideoSdkVideoRequestType.MERGE -> request.mergeData?.resultPath ?: ""
            TruvideoSdkVideoRequestType.ENCODE -> request.encodeData?.resultPath ?: ""
            TruvideoSdkVideoRequestType.CONCAT -> request.concatData?.resultPath ?: ""
        }
    }

    val inputFiles = remember(request) {
        when (request.type) {
            TruvideoSdkVideoRequestType.MERGE -> request.mergeData?.inputPaths?.map { File(it) }?.toPersistentList() ?: persistentListOf()
            TruvideoSdkVideoRequestType.CONCAT -> request.concatData?.inputPaths?.map { File(it) }?.toPersistentList() ?: persistentListOf()
            TruvideoSdkVideoRequestType.ENCODE -> {
                val path = request.encodeData?.inputPath ?: ""
                var file: File? = null
                if (path.trim().isNotEmpty()) file = File(path)
                if (file != null) {
                    persistentListOf(file)
                } else {
                    persistentListOf()
                }
            }
        }
    }


    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surfaceTint)
                        .padding(16.dp)
                ) {
                    Row {
                        Text(
                            request.type.name,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                        Text(
                            "${request.status.name} (${(request.progress * 100).toInt()}%)",
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }

                Box(modifier = Modifier.padding(8.dp)) {
                    Column {
                        Text(request.id)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            if (request.errorMessage != null)
                                Box(
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color = Color.Red)
                                        .padding(8.dp)
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
                            Box(modifier = Modifier.padding(top = 16.dp)) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Files", style = MaterialTheme.typography.titleSmall)

                                    inputFiles.forEach {
                                        key(it.path) {
                                            FileItem(path = it.path)
                                        }
                                    }
                                }

                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            if (request.status == COMPLETED && resultPath.trim().isNotEmpty()) {
                                Box(modifier = Modifier.padding(top = 16.dp)) {
                                    Column {
                                        Text("Result", style = MaterialTheme.typography.titleSmall)
                                        Box(modifier = Modifier.height(4.dp))
                                        FileItem(path = resultPath)
                                    }
                                }
                            }
                        }


                        Box(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Button(
                                enabled = buttonProcessEnabled,
                                onClick = {
                                    scope.launch {
                                        try {
                                            request.process()
                                        } catch (exception: Exception) {
                                            exception.printStackTrace()
                                        }
                                    }
                                }
                            ) {
                                Text("Process")
                            }

                            Box(modifier = Modifier.width(4.dp))

                            Button(
                                enabled = buttonCancelEnabled,
                                onClick = {
                                    scope.launch {
                                        try {
                                            request.cancel()
                                        } catch (exception: Exception) {
                                            exception.printStackTrace()
                                        }
                                    }
                                }
                            ) {
                                Text("Cancel")
                            }


                            Box(modifier = Modifier.width(4.dp))

                            Button(
                                enabled = buttonDeleteEnabled,
                                onClick = {
                                    scope.launch {
                                        try {
                                            request.delete()
                                        } catch (exception: Exception) {
                                            exception.printStackTrace()
                                        }
                                    }
                                }
                            ) {
                                Text("Delete")
                            }

                            Box(modifier = Modifier.width(4.dp))

                            Button(
                                enabled = buttonAddToFilesEnabled,
                                onClick = { addToFiles() }
                            ) {
                                Text("Add to files")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun FileItem(path: String) {
    val context = LocalContext.current

    var infoDialogVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(
                start = 8.dp,
                end = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            path,
            modifier = Modifier.weight(1f)
        )

        Box(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { infoDialogVisible = true },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = {
                val intent = Intent(context, ViewerActivity::class.java)
                intent.putExtra("filePath", path)
                context.startActivity(intent)
            },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                Icons.Outlined.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (infoDialogVisible) {
        InfoDialog(
            path = path,
            close = { infoDialogVisible = false }
        )
    }

}

@Preview
@Composable
private fun Test() {
    VideoRequestListItem(
        request = TruvideoSdkVideoRequest(
            id = "id",
            status = COMPLETED,
            createdAtMillis = System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis(),
            type = TruvideoSdkVideoRequestType.MERGE,
            errorMessage = "test error message",
            mergeData = TruvideoSdkVideoMergeRequestData(
                inputPaths = listOf("path1", "path2"),
                outputPath = "path",
                resultPath = "",
                audioTracks = listOf(),
                videoTracks = listOf(),
                width = null,
                height = null,
                framesRate = TruvideoSdkVideoFrameRate.defaultFrameRate
            )
        )
    )
}