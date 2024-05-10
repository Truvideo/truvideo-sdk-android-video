package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkVideoClearNoiseCallback {

    fun onReady(path: String)

    fun onError(exception: TruvideoSdkException)
}