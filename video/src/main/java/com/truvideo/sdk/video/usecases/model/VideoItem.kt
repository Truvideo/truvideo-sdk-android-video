package com.truvideo.sdk.video.usecases.model

import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoTrackInformation

internal data class VideoItem(
    val videoInfo: TruvideoSdkVideoInformation,
    val trackInfo: TruvideoSdkVideoTrackInformation?
)