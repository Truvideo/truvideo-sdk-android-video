package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo

class ConcatKotlin {
    suspend fun concatVideos(videos: List<String>, resultVideoPath: String) {
        try {
            val builder = TruvideoSdkVideo.ConcatBuilder(videos, resultVideoPath)
            val request = builder.build()
            request.process()

            // Handle result
            // the concated video its on 'resultVideoPath'
        } catch (exception: Exception) {
            // Handle error
            exception.printStackTrace()
        }
    }
}