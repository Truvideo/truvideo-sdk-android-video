package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkVideoAreVideosReadyToConcatCallback {

    fun onReady(boolean: Boolean)

    fun onError(exception: TruvideoSdkException)
}