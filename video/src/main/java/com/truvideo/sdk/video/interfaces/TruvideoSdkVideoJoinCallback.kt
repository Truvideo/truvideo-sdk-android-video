package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkVideoJoinCallback {

    fun onReady()

    fun onError(exception: TruvideoSdkException)
}