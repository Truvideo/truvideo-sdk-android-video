package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable

@Serializable
internal data class FfmpegExecutionResult(
    val id: Long,
    val code: Int,
    val output: String
)