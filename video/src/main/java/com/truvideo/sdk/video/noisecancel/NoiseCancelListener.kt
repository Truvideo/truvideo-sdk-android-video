package com.truvideo.sdk.video.noisecancel

interface TruvideoNoiseCancelListener {
    fun onSuccess(outputPath: String)
    fun onError()
}
