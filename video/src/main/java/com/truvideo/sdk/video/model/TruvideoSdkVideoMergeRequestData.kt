package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TruvideoSdkVideoMergeRequestData(
    val videoPaths: List<String>,
    val resultPath: String,
    val receivedWidth: Int?,
    val receivedHeight: Int?,
    val videoCodec: TruvideoSdkVideoVideoCodec,
    val framesRate: TruvideoSdkVideoFrameRate
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
