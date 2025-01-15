package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest

class ConcatKotlin {
    suspend fun concatVideos(input: List<TruvideoSdkVideoFile>, output: TruvideoSdkVideoFileDescriptor) {
        try {
            val builder = TruvideoSdkVideo.ConcatBuilder(input, output)
            val request: TruvideoSdkVideoRequest = builder.build()
            val outputPath = request.process()

            // Handle result
            // the concated video its on 'outputPath'
        } catch (exception: Exception) {
            // Handle error
            exception.printStackTrace()
        }
    }
}