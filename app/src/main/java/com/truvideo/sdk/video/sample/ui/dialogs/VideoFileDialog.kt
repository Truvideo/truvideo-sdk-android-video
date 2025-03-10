package com.truvideo.sdk.video.sample.ui.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NoiseAware
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.truvideo.sdk.video.sample.ui.components.option_dialog_content.OptionDialogContent
import com.truvideo.sdk.video.sample.ui.components.option_dialog_content.OptionDialogModel
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFileDialog(
    visible: Boolean = true,
    close: () -> Unit = {},
    onInfoPressed: () -> Unit = {},
    onPlayPressed: () -> Unit = {},
    onEditPressed: () -> Unit = {},
    onClearNoisePressed: () -> Unit = {},
    onDuplicatePressed: () -> Unit = {},
    onAddVideoTrackPressed: () -> Unit = {},
    onAddAudioTrackPressed: () -> Unit = {},
    onDeletePressed: () -> Unit = {},
    onSharePressed: () -> Unit = {},
) {
    if (visible) {
        BasicAlertDialog(
            onDismissRequest = { close() }
        ) {
            Content(
                onInfoPressed = onInfoPressed,
                onClearNoisePressed = onClearNoisePressed,
                onAddVideoTrackPressed = onAddVideoTrackPressed,
                onAddAudioTrackPressed = onAddAudioTrackPressed,
                onDuplicatePressed = onDuplicatePressed,
                onPlayPressed = onPlayPressed,
                onEditPressed = onEditPressed,
                onDeletePressed = onDeletePressed,
                onSharePressed = onSharePressed
            )
        }
    }
}

@Composable
private fun Content(
    onInfoPressed: () -> Unit = {},
    onPlayPressed: () -> Unit = {},
    onEditPressed: () -> Unit = {},
    onClearNoisePressed: () -> Unit = {},
    onDuplicatePressed: () -> Unit = {},
    onAddVideoTrackPressed: () -> Unit = {},
    onAddAudioTrackPressed: () -> Unit = {},
    onDeletePressed: () -> Unit = {},
    onSharePressed: () -> Unit = {}
) {
    OptionDialogContent(
        title = "Video",
        options = persistentListOf(
            OptionDialogModel(
                title = "Information",
                icon = Icons.Outlined.Info,
                onPressed = onInfoPressed
            ),
            OptionDialogModel(
                title = "Play",
                icon = Icons.Outlined.PlayArrow,
                onPressed = onPlayPressed
            ),
            OptionDialogModel(
                title = "Edit",
                icon = Icons.Outlined.Edit,
                onPressed = onEditPressed
            ),
            OptionDialogModel(
                title = "Clear noise",
                icon = Icons.Outlined.NoiseAware,
                onPressed = onClearNoisePressed
            ),
            OptionDialogModel(
                title = "Duplicate",
                icon = Icons.Outlined.FileCopy,
                onPressed = onDuplicatePressed
            ),
            OptionDialogModel(
                title = "Add video track",
                icon = Icons.Outlined.Videocam,
                onPressed = onAddVideoTrackPressed
            ),
            OptionDialogModel(
                title = "Add audio track",
                icon = Icons.Outlined.Audiotrack,
                onPressed = onAddAudioTrackPressed
            ),
            OptionDialogModel(
                title = "Share",
                icon = Icons.Outlined.Share,
                onPressed = onSharePressed,
            ),
            OptionDialogModel(
                title = "Delete",
                icon = Icons.Outlined.Delete,
                onPressed = onDeletePressed
            )
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun Test() {
    TruVideoSdkVideoModuleTheme {
        Content()
    }
}