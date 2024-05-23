package com.truvideo.sdk.video.usecases

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.ui.edit.TruvideoSdkVideoEditActivity
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File

internal class OpenEditScreenUseCase(
    private val authAdapter: TruvideoSdkVideoAuthAdapter
) {
    private var editors = mutableMapOf<ComponentActivity, TruvideoSdkVideoEditor>()
    fun init(activity: ComponentActivity): TruvideoSdkVideoEditor {
        var editor = editors[activity]
        if (editor == null) {
            editor = TruvideoSdkVideoEditor(activity)
            editors[activity] = editor
        }

        editor.authAdapter = authAdapter
        return editor
    }
}

class TruvideoSdkVideoEditor(
    private val activity: ComponentActivity
) {
    internal var authAdapter: TruvideoSdkVideoAuthAdapter? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var continuation: CancellableContinuation<String?>? = null
    private val startForResult =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data

                // Handle the Intent
                val videoPath: String? = intent?.getStringExtra("video")
                continuation?.resumeWith(Result.success(videoPath))
            } else {
                continuation?.resumeWith(Result.success(null))
            }
        }

    suspend fun edit(videoPath: String, resultPath: String): String? {
        authAdapter?.validateAuthentication()

        if (!File(videoPath).exists()) {
            throw TruvideoSdkVideoException("Video file not found")
        }

        val intent = Intent(activity, TruvideoSdkVideoEditActivity::class.java).apply {
            putExtra(TruvideoSdkVideoEditActivity.VIDEO_PATH_EXTRA, videoPath)
            putExtra(TruvideoSdkVideoEditActivity.VIDEO_RESULT_PATH_EXTRA, resultPath)
        }

        startForResult.launch(intent)
        return suspendCancellableCoroutine { continuation = it }
    }


    fun edit(videoPath: String, resultPath: String, callback: TruvideoSdkVideoCallback<String?>) {
        scope.launch {
            try {
                val result = edit(videoPath, resultPath)
                callback.onComplete(result)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkVideoException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }
}