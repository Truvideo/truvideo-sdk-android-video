package com.truvideo.sdk.video.ui.activities.edit

internal sealed interface EditMode {
    data object Trimming: EditMode
    data object Rotation: EditMode
    data object Sound: EditMode
}