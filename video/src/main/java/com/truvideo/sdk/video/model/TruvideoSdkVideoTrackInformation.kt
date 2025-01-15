package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TruvideoSdkVideoTrackInformation(
    val index: Long,
    val width: Int,
    val height: Int,
    val rotatedWidth: Int,
    val rotatedHeight: Int,
    val codec: String,
    val codecTag: String,
    val pixelFormat: String,
    val bitrate: Int,
    val frameRate: String,
    val rotation: TruvideoSdkVideoRotation,
    val durationMillis: Long
) {

    fun toJson() = jsonConfig.encodeToString(this)

    companion object {

        fun empty(): TruvideoSdkVideoTrackInformation {
            return TruvideoSdkVideoTrackInformation(
                index = 0,
                width = 0,
                height = 0,
                rotatedWidth = 0,
                rotatedHeight = 0,
                codec = "",
                codecTag = "",
                pixelFormat = "",
                bitrate = 0,
                frameRate = "",
                rotation = TruvideoSdkVideoRotation.DEGREES_0,
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

        fun fromJson(json: String): TruvideoSdkVideoTrackInformation = jsonConfig.decodeFromString(json)
    }
}
