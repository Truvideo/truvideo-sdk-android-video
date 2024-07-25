package com.truvideo.sdk.video.usecases

import android.util.Log
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File

internal class GenerateConcatVideosCommandUseCase {
    operator fun invoke(
        videoPaths: List<String>,
        resultPath: String,
        tempFilePath: String,
    ): String {
        try {
            val fileBuilder = StringBuilder()
            for (element in videoPaths) {
                if (fileBuilder.isNotEmpty()) {
                    fileBuilder.append("\n")
                }
                fileBuilder.append("file '$element'")
            }

            val tempFile = File(tempFilePath)
            if (tempFile.exists()) {
                tempFile.delete()
            }

            tempFile.writeText(fileBuilder.toString())
            val command = "-f concat -safe 0 -y -i ${tempFile.absolutePath} -c:v copy -c:a aac $resultPath"
            Log.d(TAG, "Command: $command")
            return command
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }

    }

    companion object {

        private const val TAG = "ConcatVideosUtils"
    }
}