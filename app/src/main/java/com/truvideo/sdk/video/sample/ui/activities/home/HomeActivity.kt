package com.truvideo.sdk.video.sample.ui.activities.home

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import com.truvideo.sdk.video.sample.ui.activities.home.components.FileListItem
import com.truvideo.sdk.video.sample.ui.activities.home.components.VideoRequestListItem
import com.truvideo.sdk.video.sample.ui.dialogs.AddVideoDialog
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import com.truvideo.sdk.video.sample.utils.AssetsUtil
import com.truvideo.sdk.video.ui.activities.edit.TruvideoSdkVideoEditContract
import com.truvideo.sdk.video.ui.activities.edit.TruvideoSdkVideoEditParams
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import java.io.File

class HomeActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TruVideoSdkVideoModuleTheme {
                Content()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Content() {
        val view = LocalView.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var files by remember { mutableStateOf<ImmutableList<TruvideoSdkVideoInformation>>(persistentListOf()) }
        var requests by remember { mutableStateOf<ImmutableList<TruvideoSdkVideoRequest>>(persistentListOf()) }
        var addFileDialogVisible by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var selectedFiles by remember { mutableStateOf<ImmutableList<String>>(persistentListOf()) }

        fun addVideo(info: TruvideoSdkVideoInformation) {
            if (files.any { it.path == info.path }) return
            files = files.toMutableList().apply { add(info) }.toPersistentList()
        }

        fun deleteVideo(info: TruvideoSdkVideoInformation) {
            try {
                File(info.path).delete()
                files = files.toMutableList().apply { remove(info) }.toPersistentList()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            selectedFiles = selectedFiles.toMutableList().apply { remove(info.path) }.toPersistentList()
        }

//        val pickFileLauncher = rememberLauncherForActivityResult(TruvideoSdkFilePickerContract()) { path: String? ->
//            Log.d("TruvideoSdkVideo", "file picked: $path")
//            if (path == null) return@rememberLauncherForActivityResult
//            val file = File(path)
//            if (files.any { it.path == file.path }) return@rememberLauncherForActivityResult
//
//            scope.launch {
//
//                if (file.exists()) {
//                    val info = try {
//                        TruvideoSdkVideo.getInfo(TruvideoSdkVideoFile.custom(file.path))
//                    } catch (exception: Exception) {
//                        exception.printStackTrace()
//                        Log.d("TruvideoSdkVideo", "Error getting video information: ${exception.message}")
//                        null
//                    }
//
//                    if (info != null) {
//                        files = files.toMutableList().apply { add(info) }.toPersistentList()
//                    }
//                }
//            }
//        }

        val editVideoLauncher = rememberLauncherForActivityResult(TruvideoSdkVideoEditContract()) { path: String? ->
            Log.d("TruvideoSdkVideo", "file edited: $path")
            if (path == null) return@rememberLauncherForActivityResult

            scope.launch {
                val info = try {
                    TruvideoSdkVideo.getInfo(TruvideoSdkVideoFile.custom(path))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    File(path).delete()
                    Log.d("TruvideoSdkVideo", "Error getting video info: ${exception.message}")
                    null
                }

                if (info == null) return@launch
                addVideo(info)
            }
        }

        fun editVideo(info: TruvideoSdkVideoInformation) {
            scope.launch {
                val file = File(info.path)
                val name = file.nameWithoutExtension

                editVideoLauncher.launch(
                    TruvideoSdkVideoEditParams(
                        input = TruvideoSdkVideoFile.custom(info.path),
                        output = TruvideoSdkVideoFileDescriptor.cache("edit_${name}")
                    )
                )
            }
        }

        fun addFilesFromAssets() {
            scope.launch {
                val names = listOf("360x240_1mb.mp4", "640x360_1mb.mp4", "720x480_1mb.mp4")
                names.forEachIndexed { index, s ->
                    val file = AssetsUtil.getFileFromAssets(
                        context = applicationContext,
                        assetName = s,
                        fileName = "file$index.mp4"
                    )
                    files = files.toMutableList().apply {
                        add(TruvideoSdkVideo.getInfo(TruvideoSdkVideoFile.custom(file.path)))
                    }.toPersistentList()

                }
            }
        }

        LaunchedEffect(Unit) {
            if (!view.isInEditMode) {
                TruvideoSdkVideo.streamAllRequests().observe(lifecycleOwner) {
                    requests = it.toPersistentList()
                }

                addFilesFromAssets()
            }
        }

        fun createMergeRequest() {
            if (selectedFiles.size < 2) return

            scope.launch {
                val request = TruvideoSdkVideo.MergeBuilder(
                    input = selectedFiles.map { TruvideoSdkVideoFile.custom(it) },
                    output = TruvideoSdkVideoFileDescriptor.cache("merge_${System.currentTimeMillis()}")
                )
                request.build()
                selectedFiles = persistentListOf()
            }
        }

        fun createConcatRequest() {
            if (selectedFiles.size < 2) return

            scope.launch {
                val request = TruvideoSdkVideo.ConcatBuilder(
                    input = selectedFiles.map { TruvideoSdkVideoFile.custom(it) },
                    output = TruvideoSdkVideoFileDescriptor.cache("concat_${System.currentTimeMillis()}")
                )
                request.build()
                selectedFiles = persistentListOf()
            }
        }

        fun createEncodeRequest() {
            if (selectedFiles.size != 1) return

            scope.launch {
                val request = TruvideoSdkVideo.EncodeBuilder(
                    input = TruvideoSdkVideoFile.custom(selectedFiles.first()),
                    output = TruvideoSdkVideoFileDescriptor.cache("encode_${System.currentTimeMillis()}")
                )
                request.build()
                selectedFiles = persistentListOf()
            }
        }

        if (addFileDialogVisible) {
            AddVideoDialog(
                close = { addFileDialogVisible = false },
                onAssetsPressed = {
                    addFileDialogVisible = false
                    addFilesFromAssets()
                },
                onPickerPressed = {
                    addFileDialogVisible = false
//                    pickFileLauncher.launch(TruvideoSdkFilePickerType.Video)
                }
            )
        }



        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                Column {
                    TopAppBar(
                        title = { Text("Video Module App") }
                    )

                    var tabIndex by remember { mutableIntStateOf(0) }

                    TabRow(selectedTabIndex = tabIndex) {
                        Tab(selected = false, onClick = { tabIndex = 0 }) {
                            Text("Files (${files.size})", modifier = Modifier.padding(16.dp))
                        }

                        Tab(selected = false, onClick = { tabIndex = 1 }) {
                            Text("Requests (${requests.size})", modifier = Modifier.padding(16.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        AnimatedContent(targetState = tabIndex, label = "") { tabIndexTarget ->
                            when (tabIndexTarget) {
                                0 -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxWidth()
                                            ) {
                                                LazyColumn(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .padding(bottom = 60.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    items(files, key = { it.path }) { item ->
                                                        FileListItem(
                                                            info = item,
                                                            checked = selectedFiles.any { it == item.path },
                                                            addFile = { addVideo(it) },
                                                            onEditPressed = { editVideo(item) },
                                                            onDeletePressed = { deleteVideo(item) },
                                                            onCheckedChange = { selected ->
                                                                if (selected) {
                                                                    if (!selectedFiles.any { it == item.path }) {
                                                                        selectedFiles =
                                                                            selectedFiles.toMutableList().apply { add(item.path) }
                                                                                .toPersistentList()
                                                                    }
                                                                } else {
                                                                    if (selectedFiles.any { it == item.path }) {
                                                                        selectedFiles =
                                                                            selectedFiles.toMutableList().apply { remove(item.path) }
                                                                                .toPersistentList()
                                                                    }
                                                                }
                                                            }
                                                        )
                                                    }
                                                }

                                                FloatingActionButton(
                                                    onClick = { addFileDialogVisible = true },
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .align(Alignment.BottomEnd)
                                                ) {
                                                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "")
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    enabled = selectedFiles.size >= 2,
                                                    onClick = { createMergeRequest() },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Merge")
                                                }

                                                Button(
                                                    enabled = selectedFiles.size >= 2,
                                                    onClick = { createConcatRequest() },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Concat")
                                                }

                                                Button(
                                                    enabled = selectedFiles.size == 1,
                                                    onClick = { createEncodeRequest() },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Encode")
                                                }
                                            }
                                        }
                                    }
                                }

                                1 -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        LazyColumn(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(requests, key = { it.id }) {
                                                VideoRequestListItem(
                                                    request = it,
                                                    addToFiles = {
                                                        scope.launch {
                                                            val path = when (it.type) {
                                                                TruvideoSdkVideoRequestType.MERGE -> it.mergeData?.resultPath ?: ""
                                                                TruvideoSdkVideoRequestType.CONCAT -> it.concatData?.resultPath ?: ""
                                                                TruvideoSdkVideoRequestType.ENCODE -> it.encodeData?.resultPath ?: ""
                                                            }
                                                            val info = TruvideoSdkVideo.getInfo(TruvideoSdkVideoFile.fromFile(File(path)))
                                                            addVideo(info)
                                                        }
                                                    }
                                                )
                                            }

                                        }
                                    }
                                }

                                else -> {

                                }
                            }
                        }
                    }

                }
            }
        }

    }

    @Composable
    @Preview(showBackground = true)
    private fun Test() {
        TruVideoSdkVideoModuleTheme {
            Content()
        }
    }
}