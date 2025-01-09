package com.truvideo.sdk.video.sample.ui.activities.home.components

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoAudioTrackInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoTrackInformation
import com.truvideo.sdk.video.sample.ui.activities.viewer.ViewerActivity
import com.truvideo.sdk.video.sample.ui.dialogs.InfoDialog
import com.truvideo.sdk.video.sample.ui.dialogs.VideoFileDialog
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FileListItem(
    info: TruvideoSdkVideoInformation,
    checked: Boolean = false,
    onEditPressed: () -> Unit = {},
    addFile: (info: TruvideoSdkVideoInformation) -> Unit = {},
    onCheckedChange: (checked: Boolean) -> Unit = {},
    onDeletePressed: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dialogVisible by remember { mutableStateOf(false) }
    var dialogInfoVisible by remember { mutableStateOf(false) }

    fun play() {
        dialogVisible = false

        val intent = Intent(context, ViewerActivity::class.java)
        intent.putExtra("filePath", info.path)
        context.startActivity(intent)
    }

    fun removeAudio() {
        scope.launch {
            Log.d("TruvideoSdkVideo", "Start to process remove audio")

            val result = try {
                val file = File(info.path)
                val extension = file.extension
                val name = file.nameWithoutExtension
                val outputPath = "${context.cacheDir.path}/${name}_muted.$extension"
                Log.d("TruvideoSdkVideo", "Remove audio output path $outputPath")

//                TruvideoSdkVideo.removeAudio(
//                    inputPath = info.path,
//                    outputPath = outputPath
//                )

                Log.d("TruvideoSdkVideo", "Audio removed")

                outputPath
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error removing audio: ${exception.message}")
                null
            }

            if (result == null) return@launch
            val newInfo = try {
                val file = TruvideoSdkVideoFile.custom(result)
                TruvideoSdkVideo.getInfo(file)
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error getting video information: ${exception.message}")
                File(result).delete()
                null
            }

            if (newInfo == null) return@launch
            addFile(newInfo)
        }
    }

    fun removeVideo() {
        scope.launch {
            Log.d("TruvideoSdkVideo", "Start to process remove video")

            val result = try {
                val file = File(info.path)
                val extension = file.extension
                val name = file.nameWithoutExtension
                val outputPath = "${context.cacheDir.path}/${name}_no_video.$extension"

//                TruvideoSdkVideo.removeVideo(
//                    inputPath = info.path,
//                    outputPath = outputPath
//                )

                Log.d("TruvideoSdkVideo", "Video removed")

                outputPath
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error removing video: ${exception.message}")
                null
            }

            if (result == null) return@launch
            val newInfo = try {
                val file = TruvideoSdkVideoFile.custom(result)
                TruvideoSdkVideo.getInfo(file)
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error getting video information: ${exception.message}")
                File(result).delete()
                null
            }

            if (newInfo == null) return@launch
            addFile(newInfo)
        }
    }

    fun duplicate() {
        scope.launch {
            val file = File(info.path)
            val name = "${file.parent ?: ""}/${file.nameWithoutExtension}_copy.${file.extension}"
            val copyFile = File(name)
            if (copyFile.exists()) copyFile.delete()
            file.copyTo(File(name))

            val newFile = TruvideoSdkVideoFile.custom(copyFile.path)
            val newInfo = TruvideoSdkVideo.getInfo(newFile)
            addFile(newInfo)
        }
    }

    fun clearNoise() {
        scope.launch {
            val outputPath = TruvideoSdkVideo.clearNoise(
                input = TruvideoSdkVideoFile.custom(info.path),
                output = TruvideoSdkVideoFileDescriptor.cache("nc_${System.currentTimeMillis()}")
            )

            val newInfo = TruvideoSdkVideo.getInfo(TruvideoSdkVideoFile.custom(outputPath))
            addFile(newInfo)
        }
    }


    fun share() {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(info.path)
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*" // Cambia esto según el tipo de archivo (e.g., "image/*" para imágenes)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "Compartir archivo"))
        }
    }

    fun addAudioTrack() {
        scope.launch {
            Log.d("TruvideoSdkVideo", "Start to process add audio")

            val result = try {
                val file = File(info.path)
                val extension = file.extension
                val name = file.nameWithoutExtension
                val outputPath = "${context.cacheDir.path}/${name}_audio.$extension"
                Log.d("TruvideoSdkVideo", "Add audio output path $outputPath")

//                TruvideoSdkVideo.addAudioTrack(
//                    inputPath = info.path,
//                    outputPath = outputPath
//                )
                Log.d("TruvideoSdkVideo", "Audio added")

                outputPath
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error adding audio track: ${exception.message}")
                null
            }

            if (result == null) return@launch
            val newInfo = try {
                val file = TruvideoSdkVideoFile.custom(result)
                TruvideoSdkVideo.getInfo(file)
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error getting video information: ${exception.message}")
                File(result).delete()
                null
            }

            if (newInfo == null) return@launch
            addFile(newInfo)
        }
    }

    fun addVideoTrack() {
        scope.launch {
            Log.d("TruvideoSdkVideo", "Start to process add video")

            val result = try {
                val file = File(info.path)
                val extension = file.extension
                val name = file.nameWithoutExtension
                val outputPath = "${context.cacheDir.path}/${name}_video.$extension"
                Log.d("TruvideoSdkVideo", "Add video output path $outputPath")

//                TruvideoSdkVideo.addVideoTrack(
//                    inputPath = info.path,
//                    outputPath = outputPath
//                )
                Log.d("TruvideoSdkVideo", "Video added")

                outputPath
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error adding video track: ${exception.message}")
                null
            }

            if (result == null) return@launch
            val newInfo = try {
                val file = TruvideoSdkVideoFile.custom(result)
                TruvideoSdkVideo.getInfo(file)
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Error getting video information: ${exception.message}")
                File(result).delete()
                null
            }

            if (newInfo == null) return@launch
            addFile(newInfo)
        }
    }

    if (dialogVisible) {
        VideoFileDialog(
            close = { dialogVisible = false },
            onInfoPressed = {
                dialogVisible = false
                dialogInfoVisible = true
            },
            onPlayPressed = {
                dialogVisible = false
                play()
            },
            onEditPressed = {
                dialogVisible = false
                onEditPressed()
            },
            onClearNoisePressed = {
                dialogVisible = false
                clearNoise()
            },
            onDuplicatePressed = {
                dialogVisible = false
                duplicate()
            },
            onRemoveAudioPressed = {
                dialogVisible = false
                removeAudio()
            },
            onRemoveVideoPressed = {
                dialogVisible = false
                removeVideo()
            },
            onDeletePressed = {
                dialogVisible = false
                onDeletePressed()
            },
            onAddAudioTrackPressed = {
                Log.d("TruvideoSdkVideo", "add audio track pressed")
                dialogVisible = false
                addAudioTrack()
            },
            onAddVideoTrackPressed = {
                dialogVisible = false
                addVideoTrack()
            },
            onSharePressed = {
                dialogVisible = false
                share()
            }
        )
    }

    if (dialogInfoVisible) {
        InfoDialog(
            path = info.path,
            close = { dialogInfoVisible = false },
        )
    }

    Card {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onCheckedChange(!checked) },
                modifier = Modifier.size(30.dp)
            ) {
                if (checked) {
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "")
                } else {
                    Icon(imageVector = Icons.Outlined.Circle, contentDescription = "")
                }
            }

            Box(Modifier.width(16.dp))

            Text(
                info.path,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )

            IconButton(
                onClick = { dialogVisible = true },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Test() {
    FileListItem(
        info = TruvideoSdkVideoInformation(
            path = "path",
            size = 0,
            durationMillis = 0,
            videoTracks = persistentListOf(
                TruvideoSdkVideoTrackInformation.empty(),
                TruvideoSdkVideoTrackInformation.empty()
            ),
            audioTracks = persistentListOf(
                TruvideoSdkVideoAudioTrackInformation.empty(),
                TruvideoSdkVideoAudioTrackInformation.empty()
            ),
            format = ""
        )
    )
}