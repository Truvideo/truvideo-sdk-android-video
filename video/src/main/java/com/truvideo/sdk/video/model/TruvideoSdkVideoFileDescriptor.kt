package com.truvideo.sdk.video.model

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class TruvideoSdkVideoFileDescriptor private constructor() {
    private var fileName: String = ""
    private var directory: TruvideoSdkVideoFileDirectory? = null
    private var rawPath: String = ""

    fun toJson() = jsonConfig.encodeToString(this)

    companion object {
        private val jsonConfig: Json
            get() {
                return Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            }

        fun fromJson(json: String): TruvideoSdkVideoFileDescriptor = jsonConfig.decodeFromString(json)

        fun cache(fileName: String): TruvideoSdkVideoFileDescriptor {
            return create(
                fileName = fileName,
                directory = TruvideoSdkVideoFileDirectory.Cache
            )
        }

        fun files(fileName: String): TruvideoSdkVideoFileDescriptor {
            return create(
                fileName = fileName,
                directory = TruvideoSdkVideoFileDirectory.Files
            )
        }

        private fun create(fileName: String, directory: TruvideoSdkVideoFileDirectory): TruvideoSdkVideoFileDescriptor {
            val file = TruvideoSdkVideoFileDescriptor()
            file.fileName = fileName
            file.directory = directory
            return file
        }

        fun custom(path: String): TruvideoSdkVideoFileDescriptor {
            val file = TruvideoSdkVideoFileDescriptor()
            file.rawPath = path
            return file
        }
    }

    internal fun getDescription(context: Context): String {
        return when (directory) {
            TruvideoSdkVideoFileDirectory.Cache -> "${context.cacheDir.path}/${fileName}"
            TruvideoSdkVideoFileDirectory.Files -> "${context.filesDir.path}/${fileName}"
            null -> rawPath
        }
    }

    internal fun getPath(context: Context, extension: String): String {
        return when (directory) {
            TruvideoSdkVideoFileDirectory.Cache -> "${context.cacheDir.path}/${fileName}.$extension"
            TruvideoSdkVideoFileDirectory.Files -> "${context.filesDir.path}/${fileName}.$extension"
            null -> "${rawPath}.$extension"
        }
    }
}

