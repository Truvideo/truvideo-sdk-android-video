package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable

@Serializable
data class TruvideoSdkVideoMergeAudioTrack(
    val tracks: List<TruvideoSdkVideoMergeMediaEntry>
)