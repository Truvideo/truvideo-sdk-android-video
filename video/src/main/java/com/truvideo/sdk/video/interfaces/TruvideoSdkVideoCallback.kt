package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkVideoCallback<T> {

    fun onComplete(result: T)

    fun onError(exception: TruvideoSdkException)
}