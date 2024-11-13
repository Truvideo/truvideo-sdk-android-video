package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TruvideoSdkVideoAudioTrackInformation(
    val index: Long,
    val codec: String,
    val codecTag: String,
    val sampleFormat: String,
    val bitrate: Int,
    val sampleRate: Int,
    val channels: Int,
    val channelLayout: String,
    val durationMillis: Long
) {
    fun toJson(): String = jsonConfig.encodeToString(this)

    companion object {

        fun empty(): TruvideoSdkVideoAudioTrackInformation {
            return TruvideoSdkVideoAudioTrackInformation(
                index = 0,
                codec = "",
                codecTag = "",
                sampleFormat = "",
                bitrate = 0,
                sampleRate = 0,
                channels = 0,
                channelLayout = "",
                durationMillis = 0
            )
        }

        private val jsonConfig: Json
            get() {
                return Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            }

        fun fromJson(json: String): TruvideoSdkVideoAudioTrackInformation = jsonConfig.decodeFromString(json)
    }
}