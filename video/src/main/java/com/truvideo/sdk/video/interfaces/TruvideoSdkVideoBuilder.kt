package com.truvideo.sdk.video.interfaces

import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest

interface TruvideoSdkVideoBuilder {
    suspend fun build(): TruvideoSdkVideoRequest

    fun build(callback: TruvideoSdkVideoCallback<TruvideoSdkVideoRequest>)
}