package com.truvideo.sdk.video.interfaces

import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkVideoBuilderCallback {
    fun onReady(request: TruvideoSdkVideoRequest)

    fun onError(exception: TruvideoSdkException)
}