package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable

@Serializable
data class TruvideoSdkVideoMergeVideoTrack(
    val tracks: List<TruvideoSdkVideoMergeMediaEntry>,
    val width: Int? = null,
    val height: Int? = null
)