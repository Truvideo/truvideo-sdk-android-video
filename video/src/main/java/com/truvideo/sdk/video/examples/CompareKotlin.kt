package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile

class CompareKotlin {
    suspend fun compareVideos(input: List<TruvideoSdkVideoFile>) {
        try {
            val result = TruvideoSdkVideo.compare(input)
            // Result is true if the videos are compatible to be concatenated, otherwise false
        } catch (exception: Exception) {
            // Handle error
            exception.printStackTrace()
        }
    }
}