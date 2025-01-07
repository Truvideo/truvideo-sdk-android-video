package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.exceptions.TruvideoSdkException

interface TruvideoSdkVideoCallback<T> {

    fun onComplete(result: T)

    fun onError(exception: TruvideoSdkException)
}