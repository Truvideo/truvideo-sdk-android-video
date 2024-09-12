package com.truvideo.sdk.video.interfaces

import com.truvideo.sdk.video.model.FfmpegExecutionResult
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation

internal interface TruvideoSdkVideoFFmpegAdapter {
    fun executeAsync(
        command: String,
        callback: (id: Long, code: Int, output: String) -> Unit
    ): Long

    fun executeArrayAsync(
        command: Array<String>,
        callback: (id: Long, code: Int, output: String) -> Unit
    ): Long

    suspend fun execute(command: String): FfmpegExecutionResult

    suspend fun executeArray(command: Array<String>): FfmpegExecutionResult

    fun cancel(executionId: Long)

    suspend fun getInformation(path: String): TruvideoSdkVideoInformation
}
