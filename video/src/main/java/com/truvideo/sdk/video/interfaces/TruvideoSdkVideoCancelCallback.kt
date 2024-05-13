package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkVideoCancelCallback {

    fun onCanceled()

    fun onError(exception: TruvideoSdkException)
}