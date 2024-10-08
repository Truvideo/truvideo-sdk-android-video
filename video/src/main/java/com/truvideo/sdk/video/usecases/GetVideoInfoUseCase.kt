package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.isSuccess
import com.truvideo.sdk.video.model.TruvideoSdkVideoAudioTrackInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import com.truvideo.sdk.video.model.TruvideoSdkVideoTrackInformation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import truvideo.sdk.common.exception.TruvideoSdkException

internal class GetVideoInfoUseCase(
    private val context: Context,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    suspend operator fun invoke(input: TruvideoSdkVideoFile): TruvideoSdkVideoInformation {
        try {
            val inputPath = input.getPath(context)
            val ffmpegSession = ffmpegAdapter.executeProbe(
                "-i $inputPath -v quiet -print_format json -show_format -show_streams -hide_banner"
            )

            if (!ffmpegSession.code.isSuccess) {
                Log.d("TruvideoSdkVideo", "Failed to get video information. ${ffmpegSession.output}")
                throw TruvideoSdkException("Unknown error")
            }

            val jsonObject = Json.parseToJsonElement(ffmpegSession.output)
            if (jsonObject !is JsonObject) {
                Log.d("TruvideoSdkVideo", "Info not valid")
                throw TruvideoSdkException("Unknown error")
            }

            val formatObject = jsonObject["format"]

            if (formatObject == null) {
                Log.d("TruvideoSdkVideo", "Format not found")
                throw TruvideoSdkException("Unknown error")
            }

            if (formatObject !is JsonObject) {
                Log.d("TruvideoSdkVideo", "Invalid format data")
                throw TruvideoSdkException("Unknown error")
            }

            val duration = ((formatObject["duration"]?.jsonPrimitive?.float ?: 0f) * 1000).toLong()
            val size = formatObject["size"]?.jsonPrimitive?.long ?: 0L
            val formatName = formatObject["format_name"]?.jsonPrimitive?.content ?: ""

            val streams = jsonObject["streams"] ?: JsonArray(listOf())
            if (streams !is JsonArray) {
                Log.d("TruvideoSdkVideo", "Streams not valid")
                throw TruvideoSdkException("Unknown error")
            }

            val videoTracks = mutableListOf<TruvideoSdkVideoTrackInformation>()
            val audioTracks = mutableListOf<TruvideoSdkVideoAudioTrackInformation>()

            streams.forEach {
                val stream = it.jsonObject
                val streamType = stream["codec_type"]?.jsonPrimitive?.content ?: ""

                when (streamType) {
                    "video" -> {
                        val index = stream["index"]?.jsonPrimitive?.long ?: 0L
                        val width = stream["width"]?.jsonPrimitive?.int ?: 0
                        val height = stream["height"]?.jsonPrimitive?.int ?: 0
                        val codecName = stream["codec_name"]?.jsonPrimitive?.content ?: ""
                        val codecTag = stream["codec_tag_string"]?.jsonPrimitive?.content ?: ""
                        val pixelFormat = stream["pix_fmt"]?.jsonPrimitive?.content ?: ""
                        val bitrate = stream["bit_rate"]?.jsonPrimitive?.int ?: 0
                        val frameRate = stream["r_frame_rate"]?.jsonPrimitive?.content ?: ""
                        val streamDuration = ((stream["duration"]?.jsonPrimitive?.float ?: 0f) * 1000).toLong()

                        var rotate = TruvideoSdkVideoRotation.DEGREES_0
                        val sideDataList = stream["side_data_list"]
                        if (sideDataList != null && sideDataList is JsonArray) {
                            for (i in 0 until sideDataList.size) {
                                val sideDataObj = sideDataList[i].jsonObject
                                val sideDataType = sideDataObj["side_data_type"]?.jsonPrimitive?.content ?: ""
                                if (sideDataType == "Display Matrix") {
                                    val rotationValue = sideDataObj["rotation"]?.jsonPrimitive?.int ?: 0
                                    rotate = when (rotationValue) {
                                        0 -> TruvideoSdkVideoRotation.DEGREES_0
                                        90 -> TruvideoSdkVideoRotation.DEGREES_270
                                        -90 -> TruvideoSdkVideoRotation.DEGREES_90
                                        180, -180 -> TruvideoSdkVideoRotation.DEGREES_180
                                        else -> TruvideoSdkVideoRotation.DEGREES_0
                                    }
                                }
                            }
                        }


                        val rotatedWidth: Int
                        val rotatedHeight: Int
                        when (rotate) {
                            TruvideoSdkVideoRotation.DEGREES_90, TruvideoSdkVideoRotation.DEGREES_270 -> {
                                rotatedWidth = height
                                rotatedHeight = width
                            }

                            else -> {
                                rotatedHeight = height
                                rotatedWidth = width
                            }
                        }

                        val model = TruvideoSdkVideoTrackInformation(
                            index = index,
                            width = width,
                            height = height,
                            rotatedWidth = rotatedWidth,
                            rotatedHeight = rotatedHeight,
                            codec = codecName,
                            codecTag = codecTag,
                            pixelFormat = pixelFormat,
                            bitrate = bitrate,
                            frameRate = frameRate,
                            durationMillis = streamDuration,
                            rotation = rotate
                        )
                        videoTracks.add(model)
                    }

                    "audio" -> {
                        val index = stream["index"]?.jsonPrimitive?.long ?: 0
                        val codecName = stream["codec_name"]?.jsonPrimitive?.content ?: ""
                        val codecTag = stream["codec_tag_string"]?.jsonPrimitive?.content ?: ""
                        val sampleFormat = stream["sample_fmt"]?.jsonPrimitive?.content ?: ""
                        val sampleRate = stream["sample_rate"]?.jsonPrimitive?.int ?: 0
                        val channels = stream["channels"]?.jsonPrimitive?.int ?: 0
                        val channelLayout = stream["channel_layout"]?.jsonPrimitive?.content ?: ""
                        val bitrate = stream["bit_rate"]?.jsonPrimitive?.int ?: 0
                        val streamDuration = ((stream["duration"]?.jsonPrimitive?.float ?: 0f) * 1000).toLong()

                        val model = TruvideoSdkVideoAudioTrackInformation(
                            index = index,
                            codec = codecName,
                            codecTag = codecTag,
                            sampleFormat = sampleFormat,
                            bitrate = bitrate,
                            durationMillis = streamDuration,
                            channels = channels,
                            channelLayout = channelLayout,
                            sampleRate = sampleRate,
                        )
                        audioTracks.add(model)
                    }
                }
            }

            val result = TruvideoSdkVideoInformation(
                durationMillis = duration,
                size = size,
                path = inputPath,
                format = formatName,
                videoTracks = videoTracks.toList(),
                audioTracks = audioTracks.toList(),
            )

            return result
        } catch (exception: Exception) {
            Log.d("TruvideoSdkVideo", "Error getting video information", exception)
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }


    companion object {
        private const val TAG = "TruvideoSdkVideo"
    }

}