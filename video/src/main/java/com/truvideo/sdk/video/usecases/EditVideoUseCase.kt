package com.truvideo.sdk.video.usecases

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import java.io.File
import java.util.Formatter

internal class EditVideoUseCase(
    private val getVideoInfoUseCase: GetVideoInfoUseCase,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {

    companion object {
        const val TAG = "TrimVideoUseCase"
    }

    suspend operator fun invoke(
        videoPath: String,
        resultPath: String,
        startPosition: Long? = null,
        endPosition: Long? = null,
        rotation: TruvideoSdkVideoRotation? = null,
        volume: Float = 1f
    ): String {
        try {
            var trimCommand = ""
            if (startPosition != null || endPosition != null) {
                val effectiveStart: Long = startPosition ?: 0L
                val effectiveEnd = if (endPosition == null) {
                    val info = getVideoInfoUseCase(videoPath)
                    info.durationMillis.toLong()
                } else {
                    endPosition
                }

                trimCommand = "-ss ${timeToFFmpeg(effectiveStart)} -to ${timeToFFmpeg((effectiveEnd))}"
            }

            var rotationMetadata = ""
            if (rotation != null) {
                rotationMetadata = "-metadata:s:v:0 rotate=${rotation.value}"
            }
            val volumeCommand = "-af \"volume=$volume\""

            val command = "-y -i $videoPath $trimCommand -c:v copy -c:a aac $rotationMetadata $volumeCommand $resultPath"
            Log.d(TAG, "Command: $command")

            val result = ffmpegAdapter.execute(command)
            if (result.code != Config.RETURN_CODE_SUCCESS) {
                throw TruvideoSdkVideoException("Error trimming video. FFmpeg code: ${result.code}")
            }

            val file = File(resultPath)
            if (!file.exists()) {
                throw TruvideoSdkVideoException("Error trimming video. This might be caused due to an inconsistent timeframe. Timeframe might be greater than video length.")
            }

            return resultPath
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkVideoException) {
                throw exception
            } else {
                throw TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun timeToFFmpeg(timeMs: Long): String {
        val totalMilliseconds = timeMs.toInt()
        val totalSeconds = totalMilliseconds / 1000
        val milliseconds = totalMilliseconds % 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val mFormatter = Formatter()
        return mFormatter.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds).toString()
    }

}