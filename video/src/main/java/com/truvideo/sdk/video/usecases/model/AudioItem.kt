package com.truvideo.sdk.video.usecases.model

import com.truvideo.sdk.video.model.TruvideoSdkVideoAudioTrackInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation

internal data class AudioItem(
    val videoInfo: TruvideoSdkVideoInformation,
    val trackInfo: TruvideoSdkVideoAudioTrackInformation?
)