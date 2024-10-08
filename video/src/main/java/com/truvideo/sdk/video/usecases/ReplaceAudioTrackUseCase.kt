package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File

internal class ReplaceAudioTrackUseCase(
    internal val context: Context,
    internal val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    companion object {
        private const val TAG = "ReplaceAudioTrackUseCase"
    }

    suspend operator fun invoke(
        videoInput: TruvideoSdkVideoFile,
        audioInput: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor
    ): String {
        val extension = File(videoInput.getPath(context)).extension
        val outputPath = output.getPath(context, extension)
        tryDeleteFile(outputPath)

        val audioCodec = when (extension) {
            "webm" -> "libopus"
            else -> "aac"
        }

        try {
            val command = arrayOf(
                "-i",
                videoInput.getPath(context),
                "-i",
                audioInput.getPath(context),
                "-c:v",
                "copy",
                "-c:a",
                audioCodec,
                "-map",
                "0:v:0",
                "-map",
                "1:a:0",
                outputPath
            )

            val execution = ffmpegAdapter.executeArray(command)
            val code = execution.code
            if (code != ExecutionResultCode.Success) {
                Log.d("TruvideoSdkVideo", "Error replacing audio track. message ${execution.output}")
                throw TruvideoSdkException("Error trying to replace the audio track. FFmpeg code: $code")
            }

            return outputPath
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }


    private fun tryDeleteFile(path: String) {
        try {
            File(path).delete()
        } catch (_: Exception) {
        }
    }
}