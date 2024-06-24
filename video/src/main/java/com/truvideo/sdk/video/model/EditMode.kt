package com.truvideo.sdk.video.model

sealed interface EditMode {
    data object Trimming: EditMode
    data object Rotation: EditMode
    data object Sound: EditMode
}