package com.truvideo.sdk.video.interfaces

import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkVideoGetVideoInfoCallback {

    fun onReady(truvideoSdkVideoInfo: TruvideoSdkVideoInformation)

    fun onError(exception: TruvideoSdkException)
}