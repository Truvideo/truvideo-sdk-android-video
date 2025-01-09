package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TruvideoSdkVideoInformation(
    val path: String,
    val size: Long,
    val durationMillis: Long,
    val format: String,
    val videoTracks: List<TruvideoSdkVideoTrackInformation>,
    val audioTracks: List<TruvideoSdkVideoAudioTrackInformation>,
) {
    fun toJson(): String = jsonConfig.encodeToString(this)

    companion object {

        fun empty(): TruvideoSdkVideoInformation {
            return TruvideoSdkVideoInformation(
                path = "",
                size = 0,
                durationMillis = 0,
                format = "",
                videoTracks = listOf(),
                audioTracks = listOf()
            )
        }

        private val jsonConfig = Json {
            ignoreUnknownKeys = true
        }

        fun fromJson(json: String): TruvideoSdkVideoInformation {
            return jsonConfig.decodeFromString(json)
        }
    }
}


