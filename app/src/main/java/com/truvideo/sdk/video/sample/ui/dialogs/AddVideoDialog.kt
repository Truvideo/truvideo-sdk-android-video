package com.truvideo.sdk.video.sample.ui.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
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
fun AddVideoDialog(
    visible: Boolean = true,
    close: () -> Unit = {},
    onAssetsPressed: () -> Unit = {},
    onPickerPressed: () -> Unit = {}
) {
    if (visible) {
        BasicAlertDialog(
            onDismissRequest = { close() }
        ) {
            Content(
                onAssetsPressed = onAssetsPressed,
                onPickerPressed = onPickerPressed
            )
        }
    }
}

@Composable
private fun Content(
    onAssetsPressed: () -> Unit = {},
    onPickerPressed: () -> Unit = {}
) {
    OptionDialogContent(
        title = "Add video",
        options = persistentListOf(
            OptionDialogModel(
                title = "Asset files",
                icon = Icons.Outlined.AttachFile,
                onPressed = onAssetsPressed
            ),
            OptionDialogModel(
                title = "File picked",
                icon = Icons.Outlined.AttachFile,
                onPressed = onPickerPressed
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