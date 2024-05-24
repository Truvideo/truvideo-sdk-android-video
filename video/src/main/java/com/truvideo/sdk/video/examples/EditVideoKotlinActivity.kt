package com.truvideo.sdk.video.examples

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.usecases.TruvideoSdkVideoEditScreen

class EditVideoKotlinActivity : ComponentActivity() {

    private var editScreen: TruvideoSdkVideoEditScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editScreen = TruvideoSdkVideo.initEditScreen(this)
    }

    suspend fun editVideo(videoPath: String, resultVideo: String) {
        val result = editScreen?.open(videoPath, resultVideo)
        // Handle result
    }
}