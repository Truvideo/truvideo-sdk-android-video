package com.truvideo.noisecancel;

interface TruvideoNoiseCancelListener {
    fun onSuccess(outputPath: String)
    fun onError()
}
