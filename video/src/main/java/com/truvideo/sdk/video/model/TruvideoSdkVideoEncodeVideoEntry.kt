package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable

@Serializable
data class TruvideoSdkVideoEncodeVideoEntry(
    val width: Int? = null,
    val height: Int? = null,
    val entryIndex: Long
)


