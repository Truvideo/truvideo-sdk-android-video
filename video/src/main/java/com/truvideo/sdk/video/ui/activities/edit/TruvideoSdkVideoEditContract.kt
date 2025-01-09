package com.truvideo.sdk.video.ui.activities.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class TruvideoSdkVideoEditContract : ActivityResultContract<TruvideoSdkVideoEditParams, String?>() {
    override fun createIntent(context: Context, input: TruvideoSdkVideoEditParams): Intent {
        return Intent(context, TruvideoSdkVideoEditActivity::class.java).apply {
            putExtra("params", input.toJson())
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.getStringExtra("path")
            else -> null
        }
    }
}