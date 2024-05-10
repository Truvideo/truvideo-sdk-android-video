package com.truvideo.sdk.video.usecases

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoEditCallback
import com.truvideo.sdk.video.ui.edit.TruvideoSdkVideoEditActivity
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File

internal class OpenEditScreenUseCase {
    private var multimediaResultFunction: TruvideoSdkVideoEditCallback? = null
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private var activity: ComponentActivity? = null

    fun init(activity: ComponentActivity) {
        this.activity = activity
        this.startForResult =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data

                    // Handle the Intent
                    val videoPath: String? = intent?.getStringExtra("video")
                    multimediaResultFunction?.onReady(videoPath)
                } else {
                    multimediaResultFunction?.onReady(null)
                }
            }
    }

    fun edit(
        videoPath: String,
        resultPath: String,
        listener: TruvideoSdkVideoEditCallback
    ) {
        if (activity == null) {
            throw TruvideoSdkException("Can't edit. Activity not initialized")
        }

        if (!File(videoPath).exists()) {
            throw TruvideoSdkException("Video file not found")
        }

        this.multimediaResultFunction = listener
        val intent = Intent(activity, TruvideoSdkVideoEditActivity::class.java).apply {
            putExtra(TruvideoSdkVideoEditActivity.VIDEO_PATH_EXTRA, videoPath)
            putExtra(TruvideoSdkVideoEditActivity.VIDEO_RESULT_PATH_EXTRA, resultPath)
        }
        startForResult.launch(intent)
    }
}