package com.truvideo.sdk.video.interfaces

import android.content.Context
import java.io.File

internal interface TruvideoSdkVideoMediaManager {

    suspend fun rotateVideo(
        context: Context,
        videoPath: String,
        videoOutputPath: String
    ): File

    suspend fun removeAudioTrack(
        context: Context,
        videoPath: String,
        videoOutputPath: String
    ): File
}