package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File

class AddVideoTrackUseCase(
    private val context: Context,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    suspend operator fun invoke(
        inputPath: String,
        outputPath: String,
        duration: Long = 10000,
        width: Int = 100,
        height: Int = 100
    ): String {
        val createdFiles = mutableListOf<File>()

        try {
            val tempPath = "${context.cacheDir.path}/temp_video_track.mp4"
            val tempFile = File(tempPath)
            if (tempFile.exists()) tempFile.delete()

//            val createVideoCommand =
//                "-y -f lavfi -i color=c=red:s=${width}x${height}:d=${duration.toFloat() / 1000} -vf \"format=yuv420p\" -c:v libx264 -r 30 -g 30 -b:v 500k ${tempFile.path}"
            val createVideoCommand =
                "-y -f lavfi -i color=c=red:s=${width}x${height}:d=${duration.toFloat() / 1000} -vf \"format=yuv420p\" -c:v libvpx -r 30 -g 30 -b:v 500k ${tempFile.path}"

            val createVideoResult = ffmpegAdapter.execute(createVideoCommand)
            if (createVideoResult.code != ExecutionResultCode.Success) {
                Log.d("TruvideoSdkVideo", "Error creating video track ${createVideoResult.output}")
                throw TruvideoSdkException("Error adding video track")
            }

            createdFiles.add(tempFile)

            val command = "-y -i $inputPath -i $tempPath -map 0:v -map 0:a -map 1:v -c:v copy -c:a copy $outputPath"
            val result = ffmpegAdapter.execute(command)
            if (result.code != ExecutionResultCode.Success) {
                Log.d("TruvideoSdkVideo", "Error adding video track ${result.output}")
                throw TruvideoSdkException("Error adding video track")
            }

            return outputPath
        } catch (exception: Exception) {
            throw exception
        } finally {
            createdFiles.forEach {
                Log.d("TruvideoSdkVideo", "Video track deleted ${it.path}")
                it.delete()
            }
        }
    }
}