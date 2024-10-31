package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor

class CleanNoiseKotlin {
    suspend fun cleanNoise(input: TruvideoSdkVideoFile, output: TruvideoSdkVideoFileDescriptor) {
        try {
            val outputPath = TruvideoSdkVideo.clearNoise(input, output)
            // Handle result
        } catch (exception: Exception) {
            // Handle error
            exception.printStackTrace()
        }
    }
}