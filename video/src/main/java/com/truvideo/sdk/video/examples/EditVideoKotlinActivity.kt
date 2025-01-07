package com.truvideo.sdk.video.examples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.ui.activities.edit.TruvideoSdkVideoEditContract
import com.truvideo.sdk.video.ui.activities.edit.TruvideoSdkVideoEditParams

class EditVideoKotlinActivity : ComponentActivity() {

    private lateinit var editVideoLauncher: ActivityResultLauncher<TruvideoSdkVideoEditParams>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editVideoLauncher = registerForActivityResult(TruvideoSdkVideoEditContract(), { resultPath ->
            // edited video its on 'resultPath'
        })
    }

    fun editVideo(input: TruvideoSdkVideoFile, output: TruvideoSdkVideoFileDescriptor) {
        editVideoLauncher.launch(
            TruvideoSdkVideoEditParams(
                input = input,
                output = output
            )
        )
    }
}