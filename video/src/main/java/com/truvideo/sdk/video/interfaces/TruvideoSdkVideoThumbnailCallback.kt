package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkVideoThumbnailCallback {

    fun onReady(path: String)

    fun onError(exception: TruvideoSdkException)
}