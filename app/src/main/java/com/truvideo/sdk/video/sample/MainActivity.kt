package com.truvideo.sdk.video.sample

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoEditCallback
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import com.truvideo.sdk.video.sample.util.RealPathUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.components.button.TruvideoButton
import truvideo.sdk.components.icon_button.TruvideoIconButton
import truvideo.sdk.components.scale_button.TruvideoScaleButton
import java.io.File

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG: String = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TruvideoSdkVideo.initEditScreen(this)

        setContent {
            TruVideoSdkVideoModuleTheme {
                Content()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
private fun Content() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    val permissionStatus = rememberMultiplePermissionsState(permissions)
    LaunchedEffect(Unit) {
        if (!permissionStatus.allPermissionsGranted) {
            permissionStatus.launchMultiplePermissionRequest()
        }
    }

    var enabled by remember { mutableStateOf(false) }
    var filesExpanded by remember { mutableStateOf(true) }
    var files by remember { mutableStateOf(listOf<File>()) }
    var requests by remember { mutableStateOf(listOf<TruvideoSdkVideoRequest>()) }

    LaunchedEffect(Unit) {
        TruvideoSdkVideo.streamAllRequests(status = null).observe(lifecycleOwner) {
            requests = it
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        val uri = it ?: return@rememberLauncherForActivityResult
        val path = RealPathUtil.realPathFromUri(context = context, uri) ?: return@rememberLauncherForActivityResult
        val file = File(path)



        CoroutineScope(Dispatchers.IO).launch {
            try {
                val info = TruvideoSdkVideo.getInfo(path)
                Log.d("TruvideoSdkVideo", info.toString())
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error $exception")
                exception.printStackTrace()
            }
        }

        if (file.exists()) {
            val newList = files.toMutableList()
            newList.add(file)
            files = newList.toList()
        }
    }


    CheckPermission(
        permissionsState = permissionStatus,
        onPermissionGranted = { enabled = true },
        onPermissionDenied = { enabled = false }
    )



    Box(modifier = Modifier.padding(16.dp)) {

        Column {

            // File list
            Box(modifier = Modifier
                .fillMaxWidth()
                .animateContentSize { _, _ -> }) {
                if (files.isNotEmpty())
                    Column {
                        TruvideoScaleButton(
                            onPressed = { filesExpanded = !filesExpanded }
                        ) {
                            Box(modifier = Modifier.padding(4.dp)) {
                                Row {
                                    Text(
                                        "${files.size} files",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (filesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = ""
                                    )
                                }
                            }

                        }


                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize { _, _ -> }
                        ) {
                            if (filesExpanded) {
                                Column {
                                    files.forEach {
                                        TruvideoScaleButton {
                                            Box(
                                                modifier = Modifier
                                                    .padding(bottom = 2.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(color = Color.Black.copy(0.1f))
                                                    .padding(4.dp)
                                            ) {
                                                Row {
                                                    Text(
                                                        it.path,
                                                        fontSize = 10.sp,
                                                        lineHeight = 10.sp,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    TruvideoIconButton(
                                                        imageVector = Icons.Default.Edit,
                                                        onPressed = {
                                                            val resultPath = "${context.filesDir.path}/edit.mp4"
                                                            TruvideoSdkVideo.openEditScreen(
                                                                it.path,
                                                                resultPath,
                                                                object : TruvideoSdkVideoEditCallback {
                                                                    override fun onReady(path: String?) {
                                                                        if (path != null) {
                                                                            File(path).delete()
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.height(16.dp))
                    }
            }

            TruvideoButton(
                enabled = enabled,
                text = "Add file",
                onPressed = { filePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }
            )

            Box(Modifier.height(8.dp))

            Row {
                Box(Modifier.weight(1f)) {
                    TruvideoButton(
                        enabled = files.size >= 2,
                        text = "Concat",
                        onPressed = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val time = System.currentTimeMillis()
                                val resultPath = "${context.filesDir.path}/concat_$time.mp4"
                                val builder = TruvideoSdkVideo.ConcatBuilder(
                                    videoPaths = files.map { it.path },
                                    resultPath = resultPath
                                )
                                builder.build()
                            }
                        }
                    )
                }
                Box(modifier = Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    TruvideoButton(
                        enabled = files.size >= 2,
                        text = "Merge",
                        onPressed = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val time = System.currentTimeMillis()
                                val resultPath = "${context.filesDir.path}/merge_$time.mp4"
                                val builder = TruvideoSdkVideo.MergeBuilder(
                                    videoPaths = files.map { it.path },
                                    resultPath = resultPath
                                )
                                builder.build()
                            }
                        }
                    )
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPermission(
    permissionsState: MultiplePermissionsState,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: (text: String) -> Unit
) {
    if (permissionsState.allPermissionsGranted) {
        onPermissionGranted()
    } else {
        onPermissionDenied("")
    }
}
