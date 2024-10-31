package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TruvideoSdkVideoMergeRequestData(
    val inputPaths: List<String>,
    val outputPath: String,
    val resultPath: String,
    val width: Int?,
    val height: Int?,
    val framesRate: TruvideoSdkVideoFrameRate,
    val videoTracks: List<TruvideoSdkVideoMergeVideoTrack>,
    val audioTracks: List<TruvideoSdkVideoMergeAudioTrack>
) {
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String): TruvideoSdkVideoMergeRequestData {
            val jsonConfig = Json {
                ignoreUnknownKeys = true
            }
            return jsonConfig.decodeFromString(json)
        }
    }
}
