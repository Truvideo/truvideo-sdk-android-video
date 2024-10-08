package com.truvideo.sdk.video.ui.activities.edit

import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TruvideoSdkVideoEditParams(
    val input: TruvideoSdkVideoFile,
    val output: TruvideoSdkVideoFileDescriptor
) {
    fun toJson() = jsonConfig.encodeToString(this)

    companion object {

        private val jsonConfig: Json
            get() {
                return Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            }

        fun fromJson(json: String): TruvideoSdkVideoEditParams = jsonConfig.decodeFromString(json)
    }
}