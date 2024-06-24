package com.truvideo.sdk.video.interfaces

import com.truvideo.sdk.video.model.TruvideoSdkVideoException

interface TruvideoSdkVideoCallback<T> {

    fun onComplete(result: T)

    fun onError(exception: TruvideoSdkVideoException)
}