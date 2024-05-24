package com.truvideo.sdk.video.adapters

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.Level
import com.truvideo.sdk.video.interfaces.ExecutionResult
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.suspendCoroutine


internal class TruvideoSdkVideoFFmpegAdapterImpl : TruvideoSdkVideoFFmpegAdapter {

    companion object {
        const val TAG = "FFmpegAdapterImpl"
    }

    init {
        Config.setLogLevel(Level.AV_LOG_DEBUG)
        Config.enableLogCallback { }
    }

    override fun executeAsync(
        command: String, callback: (id: Long, code: Int, output: String) -> Unit
    ): Long {
        return FFmpeg.executeAsync(command) { id, code ->
            val output = Config.getLastCommandOutput()
            callback(id, code, output)
        }
    }

    override fun executeArrayAsync(
        command: Array<String>, callback: (id: Long, code: Int, output: String) -> Unit
    ): Long {
        return FFmpeg.executeAsync(command) { id, code ->
            val output = Config.getLastCommandOutput()
            callback(id, code, output)
        }
    }

    override suspend fun execute(command: String): ExecutionResult {
        return suspendCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
                executeAsync(
                    command,
                    callback = { id, code, output ->
                        cont.resumeWith(
                            Result.success(
                                ExecutionResult(
                                    id, code, output
                                )
                            )
                        )
                    },
                )
            }
        }
    }

    override suspend fun executeArray(command: Array<String>): ExecutionResult {
        return suspendCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
                executeArrayAsync(
                    command,
                    callback = { id, code, output ->
                        cont.resumeWith(
                            Result.success(
                                ExecutionResult(
                                    id, code, output
                                )
                            )
                        )
                    },
                )
            }
        }
    }


    override fun cancel(executionId: Long) {
        try {
            FFmpeg.cancel(executionId)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun getInformation(path: String): TruvideoSdkVideoInformation =
        processGetInformation(path, 0)

    private suspend fun processGetInformation(
        path: String, retry: Int = 0
    ): TruvideoSdkVideoInformation {
        try {
            val result = execute("-y -i $path")
            val output = result.output
            val lines = output.trim().split("\n")

            val outputLine = lines.firstOrNull { it.contains("Output #") }
            if (outputLine != null) {
                throw TruvideoSdkVideoException("Invalid output")
            }

            val durationLine = lines.firstOrNull { it.contains("Duration:") }
                ?: throw TruvideoSdkVideoException("Video duration not found")
            val videoDuration =
                convertDuration(durationLine.split(",").first().replace("Duration: ", "").trim())

            val streams = lines.filter { it.trim().startsWith("Stream #") }.map { it.trim() }

            var videoCodec = ""
            var videoPixelFormat = ""
            var videoWidth = 0
            var videoHeight = 0
            var rotation = 0

            var audioCodec = ""
            var audioSampleRate = 0

            val regex = Regex(", (?![^()]*\\))")

            streams.forEach { stream ->
                if (stream.contains("Video: ")) {
                    val parts = stream.split(regex)
                    videoCodec = parts[2].split(":").last().trim()
                    videoPixelFormat = parts[4].split("(").first()
                    videoWidth = parts[5].split("(").first().split("x")[0].trim().toInt()
                    videoHeight = parts[5].split("(").first().split("x")[1].trim().toInt()
                }

                if (stream.contains("Audio: ")) {
                    val parts = stream.split(regex)
                    audioCodec = parts[2].split(":").last().split("(").first().trim()
                    audioSampleRate = parts[3].split(" ").first().trim().toInt()
                }
            }

            val rotateLine = lines.find { it.trim().startsWith("rotate") }
            if (rotateLine != null) {
                val rotateMatch = Regex("rotate\\s*:\\s*(\\d+)").find(rotateLine)
                if (rotateMatch != null) {
                    rotation = rotateMatch.groupValues[1].toIntOrNull() ?: 0
                }
            }

            return TruvideoSdkVideoInformation(
                withVideo = videoCodec.trim().isNotEmpty(),
                videoCodec = videoCodec,
                videoPixelFormat = videoPixelFormat,
                width = videoWidth,
                height = videoHeight,
                size = File(path).length(),
                path = path,
                audioCodec = audioCodec,
                audioSampleRate = audioSampleRate,
                durationMillis = videoDuration,
                withAudio = audioCodec.trim().isNotEmpty(),
                rotation = rotation
            )
        } catch (exception: Exception) {
            Log.d("TruvideoSdkVideo", "Error getting video information", exception)
            exception.printStackTrace()

            val count = retry + 1
            if (count < 5) {
                Log.d("TruvideoSdkVideo", "Retry $count")
                delay(100)
                return processGetInformation(path, count)
            }

            throw TruvideoSdkVideoException("Error getting video information")
        }
    }

    private fun convertDuration(timeString: String): Int {
        val parts = timeString.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val seconds = parts[2].split(".")[0].toInt()
        val milliseconds = parts[2].split(".")[1].toInt()
        return (hours * 3600 + minutes * 60 + seconds) * 1000 + milliseconds
    }
}