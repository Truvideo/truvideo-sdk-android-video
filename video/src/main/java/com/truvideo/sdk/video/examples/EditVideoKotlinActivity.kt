package com.truvideo.sdk.video.examples

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.usecases.TruvideoSdkVideoEditor

class EditVideoKotlinActivity : ComponentActivity() {

    private var videoEditor: TruvideoSdkVideoEditor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoEditor = TruvideoSdkVideo.initEditor(this)
    }

    suspend fun editVideo(videoPath: String, resultVideo: String) {
        val result = videoEditor?.edit(videoPath, resultVideo)

        // Handle result
    }
}