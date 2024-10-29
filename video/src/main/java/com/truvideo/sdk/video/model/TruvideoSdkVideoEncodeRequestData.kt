package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TruvideoSdkVideoEncodeRequestData(
    val inputPath: String,
    val outputPath: String,
    val resultPath: String,
    val width: Int?,
    val height: Int?,
    val framesRate: TruvideoSdkVideoFrameRate,
    val videoTracks: List<TruvideoSdkVideoEncodeVideoEntry>,
    val audioTracks: List<Long>
) {
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String): TruvideoSdkVideoEncodeRequestData {
            val jsonConfig = Json {
                ignoreUnknownKeys = true
            }
            return jsonConfig.decodeFromString(json)
        }
    }
}
