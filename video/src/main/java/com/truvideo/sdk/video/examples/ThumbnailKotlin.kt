package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo

class ThumbnailKotlin {
    suspend fun generateThumbnail(videoPath: String, resultImagePath: String) {
        try {
            TruvideoSdkVideo.createThumbnail(
                videoPath = videoPath,
                resultPath = resultImagePath,
                position = 1000,
                width = 300, // or null
                height = 300 // or null
            )

            // Handle result
            // the thumbnail image is stored in resultImagePath
        } catch (exception: Exception) {
            // Handle error
            exception.printStackTrace()
        }
    }
}