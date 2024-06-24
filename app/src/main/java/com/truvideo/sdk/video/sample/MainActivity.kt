package com.truvideo.sdk.video.sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.components.button.TruvideoButton
import com.truvideo.sdk.components.scale_button.TruvideoScaleButton
import com.truvideo.sdk.core.TruvideoSdk
import com.truvideo.sdk.core.usecases.TruvideoSdkFilePicker
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import com.truvideo.sdk.video.usecases.TruvideoSdkVideoEditScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG: String = "MainActivity"
    }

    private lateinit var filePicker: TruvideoSdkFilePicker
    private lateinit var videoEditScreen: TruvideoSdkVideoEditScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePicker = TruvideoSdk.initFilePicker(this)
        videoEditScreen = TruvideoSdkVideo.initEditScreen(this)

        setContent {
            TruVideoSdkVideoModuleTheme {
                Content()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Content() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var filesExpanded by remember { mutableStateOf(true) }
        var files by remember { mutableStateOf(listOf<TruvideoSdkVideoInformation>()) }
        var requests by remember { mutableStateOf(listOf<TruvideoSdkVideoRequest>()) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            TruvideoSdkVideo.streamAllRequests(status = null).observe(lifecycleOwner) {
                requests = it
            }
        }

        fun openFilePicker() {
            scope.launch {
                val fileNames = listOf("PX_1.mp4", "PX_2.mp4")
                fileNames.forEach {
                    val file = getFileFromAssets(applicationContext, it, TruvideoSdkVideoRotation.DEGREES_90)
                    val info = TruvideoSdkVideo.getInfo(file.path)
                    files = files.toMutableList().apply { add(info) }.toList()
                }

//            scope.launch {
//                val path = filePicker.pick(TruvideoSdkFilePickerType.Video) ?: return@launch
//                val file = File(path)
//                if (file.exists()) {
//                    val newList = files.toMutableList()
//                    newList.add(file)
//                    files = newList.toList()
//                }
//            }
            }
        }

        fun editVideo(info: TruvideoSdkVideoInformation) {
            scope.launch {
                val path = videoEditScreen.open(
                    videoPath = info.path,
                    resultPath = "${context.filesDir.path}/${System.currentTimeMillis()}.mp4"
                )

                if (path != null) {
                    files = files.map {
                        if (it.path == info.path) {
                            TruvideoSdkVideo.getInfo(path)
                        } else {
                            it
                        }
                    }.toList()
                    File(info.path).delete()
                }
            }
        }

        Box(modifier = Modifier.padding(16.dp)) {

            Column {

                // File list
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    if (files.isNotEmpty()) Column {
                        TruvideoScaleButton(onPressed = { filesExpanded = !filesExpanded }) {
                            Box(modifier = Modifier.padding(4.dp)) {
                                Row {
                                    Text(
                                        "${files.size} files", modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (filesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = ""
                                    )
                                }
                            }

                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            if (filesExpanded) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    files.forEach {
                                        FileListItem(
                                            info = it,
                                            onEditPressed = { editVideo(it) }
                                        )
                                    }

                                    Box(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                TruvideoButton(text = "Add file", onPressed = { openFilePicker() })

                Box(Modifier.height(8.dp))

                Row {
                    Box(Modifier.weight(1f)) {
                        TruvideoButton(enabled = files.size >= 2, text = "Concat", onPressed = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val time = System.currentTimeMillis()
                                val resultPath = "${context.filesDir.path}/concat_$time.mp4"
                                val builder = TruvideoSdkVideo.ConcatBuilder(
                                    videoPaths = files.map { it.path }, resultPath = resultPath
                                )
                                builder.build()
                            }
                        })
                    }
                    Box(modifier = Modifier.width(8.dp))
                    Box(Modifier.weight(1f)) {
                        TruvideoButton(enabled = files.size >= 2, text = "Merge", onPressed = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val time = System.currentTimeMillis()
                                val resultPath = "${context.filesDir.path}/merge_$time.mp4"
                                val builder = TruvideoSdkVideo.MergeBuilder(
                                    videoPaths = files.map { it.path }, resultPath = resultPath
                                )
                                builder.build()
                            }
                        })
                    }
                }

                Box(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(requests) {
                            key(it.id) {
                                Box(
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .padding(bottom = 8.dp)
                                ) {
                                    VideoRequestListItem(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


private suspend fun getFileFromAssets(
    context: Context,
    fileName: String,
    rotation: TruvideoSdkVideoRotation? = null
): TruvideoSdkVideoInformation {
    return suspendCoroutine {
        CoroutineScope(Dispatchers.IO).launch {
            val assetManager = context.assets
            val outFile = File(context.cacheDir, fileName)

            try {
                val inputStream = assetManager.open(fileName)
                val outputStream = FileOutputStream(outFile)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }


            val resultPath = TruvideoSdkVideo.edit(
                videoPath = outFile.path,
                resultPath = "${context.cacheDir.path}/edit_$fileName",
                rotation = rotation
            )

            outFile.delete()

            val info = TruvideoSdkVideo.getInfo(resultPath)
            Log.d("TruvideoSdkVideo", "Video rotation: ${info.rotation}")
            it.resumeWith(Result.success(info))
        }
    }
}

