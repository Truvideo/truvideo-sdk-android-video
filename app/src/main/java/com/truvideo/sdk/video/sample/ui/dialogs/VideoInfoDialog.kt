package com.truvideo.sdk.video.sample.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoAudioTrackInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoTrackInformation
import com.truvideo.sdk.video.sample.ui.components.json.CustomJsonViewer
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDialog(
    visible: Boolean = true,
    close: () -> Unit = {},
    path: String
) {
    if (visible) {
        var info by remember { mutableStateOf<TruvideoSdkVideoInformation?>(null) }
        LaunchedEffect(key1 = path) {
            info = try {
                val file = TruvideoSdkVideoFile.custom(path)
                TruvideoSdkVideo.getInfo(file)
            } catch (exception: Exception) {
                exception.printStackTrace()
                null
            }
        }

        BasicAlertDialog(
            onDismissRequest = { close() }
        ) {
            Content(info = info)
        }
    }
}

@Composable
private fun Content(
    info: TruvideoSdkVideoInformation?
) {
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = AlertDialogDefaults.TonalElevation
    ) {

        AnimatedContent(targetState = info, label = "") { infoTarget ->
            if (infoTarget != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    CustomJsonViewer(
                        data = infoTarget.toJson()
                    )
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

    }
}

@Composable
@Preview(showBackground = true)
private fun Test() {
    val info = remember {
        TruvideoSdkVideoInformation(
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
    }
    var infoVisible by remember { mutableStateOf(false) }

    TruVideoSdkVideoModuleTheme {

        Column {
            Content(
                info = if (infoVisible) info else null
            )
            Text("Visible: $infoVisible", Modifier.clickable {
                infoVisible = !infoVisible
            })
        }
    }

}