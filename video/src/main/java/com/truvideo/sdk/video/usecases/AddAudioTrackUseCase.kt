package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File

class AddAudioTrackUseCase(
    private val context: Context,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    suspend operator fun invoke(
        inputPath: String,
        resultPath: String,
        duration: Long = 10000
    ): String {
        val createdFiles = mutableListOf<File>()

        try {
            val tempPath = "${context.cacheDir.path}/temp_audio_track.mp4"
            val tempFile = File(tempPath)
            if (tempFile.exists()) tempFile.delete()

            val durationSeconds = duration.toFloat() / 1000
            val createTrackCommand =
                "-y -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100:duration=$durationSeconds -c:a aac ${tempFile.path}"
            val createTrackResult = ffmpegAdapter.execute(createTrackCommand)
            if (createTrackResult.code != ExecutionResultCode.Success) {
                Log.d("TruvideoSdkVideo", "Error creating audio track ${createTrackResult.output}")
                throw TruvideoSdkException("Error adding audio track")
            }

            createdFiles.add(tempFile)

            val command = "-y -i $inputPath -i $tempPath -map 0:v -map 0:a -map 1:a -c:v copy -c:a copy $resultPath"
            val result = ffmpegAdapter.execute(command)
            if (result.code != ExecutionResultCode.Success) {
                Log.d("TruvideoSdkVideo", "Error adding audio track ${result.output}")
                throw TruvideoSdkException("Error adding audio track")
            }

            return resultPath
        } catch (exception: Exception) {
            throw exception
        } finally {
            createdFiles.forEach {
                Log.d("TruvideoSdkVideo", "Temp audio track deleted ${it.path}")
                it.delete()
            }
        }
    }
}