package com.truvideo.sdk.video.model

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class TruvideoSdkVideoFile private constructor() {
    private var fileName: String = ""
    private var extension: String = ""
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

        @JvmStatic
        fun fromJson(json: String): TruvideoSdkVideoFile = jsonConfig.decodeFromString(json)

        @JvmStatic
        fun cache(fileName: String, extension: String): TruvideoSdkVideoFile {
            return create(
                fileName = fileName,
                extension = extension,
                directory = TruvideoSdkVideoFileDirectory.Cache
            )
        }

        @JvmStatic
        fun files(fileName: String, extension: String): TruvideoSdkVideoFile {
            return create(
                fileName = fileName,
                extension = extension,
                directory = TruvideoSdkVideoFileDirectory.Files
            )
        }

        @JvmStatic
        private fun create(fileName: String, extension: String, directory: TruvideoSdkVideoFileDirectory): TruvideoSdkVideoFile {
            val file = TruvideoSdkVideoFile()
            file.fileName = fileName
            file.extension = extension
            file.directory = directory
            return file
        }

        @JvmStatic
        fun fromFile(file: File): TruvideoSdkVideoFile = custom(file.path)

        @JvmStatic
        fun custom(path: String): TruvideoSdkVideoFile {
            val file = TruvideoSdkVideoFile()
            file.rawPath = path
            return file
        }
    }

    fun getPath(context: Context): String {
        return when (directory) {
            TruvideoSdkVideoFileDirectory.Cache -> "${context.cacheDir.path}/${fileName}.$extension"
            TruvideoSdkVideoFileDirectory.Files -> "${context.filesDir.path}/${fileName}.$extension"
            null -> rawPath
        }
    }
}

