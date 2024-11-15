package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor

class ThumbnailKotlin {
    suspend fun generateThumbnail(input: TruvideoSdkVideoFile, output: TruvideoSdkVideoFileDescriptor) {
        try {
            val resultPath: String = TruvideoSdkVideo.createThumbnail(
                input = input,
                output = output,
                position = 1000,
                width = 300, // or null
                height = 300 // or null
            )

            // Handle result
            // the thumbnail image is stored in resultPath
        } catch (exception: Exception) {
            // Handle error
            exception.printStackTrace()
        }
    }
}