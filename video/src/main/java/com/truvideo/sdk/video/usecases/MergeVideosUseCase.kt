package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import android.util.Size
import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.isSuccess
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoFrameRate
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoMergeAudioTrack
import com.truvideo.sdk.video.model.TruvideoSdkVideoMergeVideoTrack
import com.truvideo.sdk.video.usecases.model.AudioItem
import com.truvideo.sdk.video.usecases.model.AudioTrack
import com.truvideo.sdk.video.usecases.model.VideoItem
import com.truvideo.sdk.video.usecases.model.VideoTrack
import truvideo.sdk.common.exceptions.TruvideoSdkException

internal class MergeVideosUseCase(
    private val context: Context,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter,
    private val getVideoInfoUseCase: GetVideoInfoUseCase
) {
    suspend operator fun invoke(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor,
        width: Int?,
        height: Int?,
        videoTracks: List<TruvideoSdkVideoMergeVideoTrack>,
        audioTracks: List<TruvideoSdkVideoMergeAudioTrack>,
        frameRate: TruvideoSdkVideoFrameRate,
        printLogs: Boolean = true,
    ): String {
        try {
            val command = generateCommand(
                input = input,
                output = output,
                width = width,
                height = height,
                frameRate = frameRate,
                videoTracks = videoTracks,
                audioTracks = audioTracks,
                printLogs = printLogs
            )

            val sessionResult = ffmpegAdapter.execute(command)
            if (!sessionResult.code.isSuccess) {
                if (printLogs) {
                    Log.d("TruvideoSdkVideo", "Failed merging videos. ${sessionResult.output}")
                }
                throw TruvideoSdkException("Unknown error")
            }

            val outputPath = output.getPath(context, "mp4")
            return outputPath
        } catch (exception: Exception) {
            Log.d("TruvideoSdkVideo", "Failed merging videos. ${exception.localizedMessage}")

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    suspend fun mergeAsync(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor,
        width: Int?,
        height: Int?,
        frameRate: TruvideoSdkVideoFrameRate,
        videoTracks: List<TruvideoSdkVideoMergeVideoTrack>,
        audioTracks: List<TruvideoSdkVideoMergeAudioTrack>,
        onRequestCreated: (id: Long) -> Unit = {},
        progressCallback: (progress: Long) -> Unit = {},
        callback: (result: String) -> Unit = {},
        callbackCanceled: () -> Unit = {},
        callbackError: (exception: TruvideoSdkException) -> Unit = {},
        printLogs: Boolean = true
    ) {
        try {
            val command = generateCommand(
                input = input,
                output = output,
                width = width,
                height = height,
                frameRate = frameRate,
                videoTracks = videoTracks,
                audioTracks = audioTracks,
                printLogs = printLogs
            )

            ffmpegAdapter.executeAsync(
                command = command,
                callback = {
                    when (it.code) {
                        ExecutionResultCode.Success -> {
                            if (printLogs) {
                                Log.d("TruvideoSdkVideo", "Successfully merged videos")
                            }
                            val outputPath = output.getPath(context, "mp4")
                            callback(outputPath)
                        }

                        ExecutionResultCode.Canceled -> {
                            if (printLogs) {
                                Log.d("TruvideoSdkVideo", "Canceled merging videos")
                            }
                            callbackCanceled()
                        }

                        ExecutionResultCode.Error -> {
                            if (printLogs) {
                                Log.d("TruvideoSdkVideo", "Failed merging videos. ${it.output}")
                            }
                            callbackError(TruvideoSdkException("Unknown error"))
                        }
                    }
                },
                progressCallback = { progressCallback(it) },
                onRequestCreated = { onRequestCreated(it) }
            )
        } catch (exception: Exception) {
            Log.d("TruvideoSdkVideo", "Failed merging videos. ${exception.localizedMessage}")
            if (exception is TruvideoSdkException) {
                callbackError(exception)
            } else {
                callbackError(TruvideoSdkException("Unknown error"))
            }
        }
    }

    private suspend fun generateCommand(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor,
        width: Int?,
        height: Int?,
        frameRate: TruvideoSdkVideoFrameRate,
        videoTracks: List<TruvideoSdkVideoMergeVideoTrack>,
        audioTracks: List<TruvideoSdkVideoMergeAudioTrack>,
        printLogs: Boolean = false,
    ): String {
        val videosInfo = input.map { getVideoInfoUseCase(it) }.toList()

        fun printLog(message: String) {
            if (!printLogs) return
            Log.d("TruvideoSdkVideo", message)
        }

        videoTracks.forEach {
            printLog("Video track. Resolution: ${it.width}x${it.height}")
        }

        // Convert videos
        val effectiveVideoTracks = convertVideoInput(
            videosInfo = videosInfo,
            data = videoTracks,
            width = width,
            height = height
        )
        if (printLogs) {
            effectiveVideoTracks.forEach {
                printLog("Video track. Resolution: ${it.width}x${it.height}")
                it.tracks.forEach { videoEntry ->
                    printLog("File: \"${videoEntry.videoInfo.path}\". Track duration: ${videoEntry.trackInfo?.durationMillis}")
                }
            }
        }

        // Convert audios
        val effectiveAudioTracks = convertAudioInput(
            videosInfo = videosInfo,
            data = audioTracks
        )
        effectiveAudioTracks.forEach {
            printLog("Audio track.")
            it.tracks.forEach { videoEntry ->
                printLog("File: \"${videoEntry.videoInfo.path}\". Track duration: ${videoEntry.trackInfo?.durationMillis}")
            }
        }

        if (effectiveVideoTracks.isEmpty() && effectiveAudioTracks.isEmpty()) {
            throw TruvideoSdkException("No video or audio tracks")
        }

        val command = buildString {
            append("-y ") // Overwrite output file if it exists

            // Add inputs and select the first video and audio stream from each file
            input.forEach { file ->
                append("-i \"${file.getPath(context)}\" ")
            }

            val filterParts = mutableListOf<String>()

            // Video tracks
            val outVideos = mutableListOf<String>()
            effectiveVideoTracks.forEachIndexed { videoTrackIndex, videoTrack ->
                var videoCounter = 0
                var videoTrackDuration = 0L
                val videoTrackNames = mutableListOf<String>()

                val effectiveWidth = videoTrack.width
                val effectiveHeight = videoTrack.height

                videoTrack.tracks.forEach { entry ->
                    val track = entry.trackInfo
                    val videoInfo = entry.videoInfo
                    val fileIndex = input.indexOfFirst { it.getPath(context) == entry.videoInfo.path }

                    if (track != null) {
                        val videoIndex = videoInfo.videoTracks.indexOfFirst { it.index == track.index }

                        val trackName = "[v_track${videoTrackIndex}_video${videoCounter}]"
                        videoTrackNames.add(trackName)
                        printLog("Video track $videoTrackIndex. New video $trackName. Video track of ${track.durationMillis}ms found on file $fileIndex")

                        val scale =
                            "'if(gt(iw/ih,$effectiveWidth/$effectiveHeight),$effectiveWidth,-2)':'if(gt(iw/ih,$effectiveWidth/$effectiveHeight),-2,$effectiveHeight)'"
                        val pad = "$effectiveWidth:$effectiveHeight:(ow-iw)/2:(oh-ih)/2"
                        filterParts.add("[${fileIndex}:v:$videoIndex]scale=$scale,pad=$pad,setsar=1$trackName")
                        videoCounter++
                        videoTrackDuration += track.durationMillis

                        // rest
                        val rest = videoInfo.durationMillis - track.durationMillis
                        if (rest > 0) {
                            val restTrackName = "[v_track${videoTrackIndex}_video${videoCounter}]"
                            videoTrackNames.add(restTrackName)
                            printLog("Video track $videoTrackIndex. New video $restTrackName. Creating a video of ${rest}ms to fulfill the file $fileIndex duration of ${videoInfo.durationMillis}ms")

                            filterParts.add("color=c=black:s=${effectiveWidth}x${effectiveHeight}:d=${rest}ms$restTrackName")
                            videoCounter++
                            videoTrackDuration += rest
                        }
                    } else {
                        val trackName = "[v_track${videoTrackIndex}_video${videoCounter}]"
                        videoTrackNames.add(trackName)
                        printLog("Video track $videoTrackIndex. New video $trackName. Video not found on file $fileIndex. Creating a video of ${videoInfo.durationMillis}ms")

                        filterParts.add("color=c=black:s=${effectiveWidth}x${effectiveHeight}:d=${videoInfo.durationMillis}ms$trackName")
                        videoCounter++
                        videoTrackDuration += videoInfo.durationMillis
                    }
                }

                printLog("Video track $videoTrackIndex finished. Total duration ${videoTrackDuration}ms")

                if (videoTrackNames.isNotEmpty()) {
                    val count = videoTrackNames.size
                    val name = "[v_track$videoTrackIndex]"
                    outVideos.add(name)

                    filterParts.add("${videoTrackNames.joinToString("")}concat=n=$count:v=1:a=0$name")
                }
            }


            // Audio tracks
            val outAudios = mutableListOf<String>()
            effectiveAudioTracks.forEachIndexed { audioTrackIndex, audioTrack ->
                var audioCounter = 0
                var audioTrackDuration = 0L
                val audioTrackNames = mutableListOf<String>()

                audioTrack.tracks.forEach { entry ->
                    val track = entry.trackInfo
                    val videoInfo = entry.videoInfo
                    val fileIndex = input.indexOfFirst { it.getPath(context) == entry.videoInfo.path }

                    if (track != null) {
                        val audioIndex = videoInfo.audioTracks.indexOfFirst { it.index == track.index }

                        val trackName = "[a_track${audioTrackIndex}_audio${audioCounter}]"
                        audioTrackNames.add(trackName)

                        printLog("Audio track $audioTrackIndex. New audio $trackName. Audio track of ${track.durationMillis}ms found on file $fileIndex")
                        filterParts.add("[${fileIndex}:a:$audioIndex]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo$trackName")
                        audioCounter++
                        audioTrackDuration += track.durationMillis

                        // rest
                        val rest = videoInfo.durationMillis - track.durationMillis
                        if (rest > 0) {
                            val restTrackName = "[a_track${audioTrackIndex}_audio${audioCounter}]"
                            audioTrackNames.add(restTrackName)

                            printLog("Audio track $audioTrackIndex. New audio $restTrackName. Creating an audio of ${rest}ms to fulfill the file $fileIndex duration of ${videoInfo.durationMillis}ms")

                            val tempTrackName = "[a_track${audioTrackIndex}_audio${audioCounter}_rest]"
                            filterParts.add("aevalsrc=0:s=44100:d=${rest}ms$tempTrackName")
                            filterParts.add("${tempTrackName}aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo$restTrackName")
                            audioCounter++
                            audioTrackDuration += rest
                        }
                    } else {
                        val trackName = "[a_track${audioTrackIndex}_audio${audioCounter}]"
                        audioTrackNames.add(trackName)

                        printLog("Audio track $audioTrackIndex. New audio $trackName. Audio not found on file $fileIndex. Creating an audio of ${videoInfo.durationMillis}ms")

                        val tempTrackName = "[a_track${audioTrackIndex}_audio${audioCounter}_rest]"
                        filterParts.add("aevalsrc=0:s=44100:d=${videoInfo.durationMillis}ms$tempTrackName")
                        filterParts.add("${tempTrackName}aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo$trackName")
                        audioCounter++
                        audioTrackDuration += videoInfo.durationMillis
                    }
                }

                printLog("Audio track $audioTrackIndex finished. Total duration ${audioTrackDuration}ms")

                if (audioTrackNames.isNotEmpty()) {
                    val count = audioTrackNames.size
                    val name = "[a_track$audioTrackIndex]"
                    outAudios.add(name)

                    filterParts.add("${audioTrackNames.joinToString("")}concat=n=$count:v=0:a=1$name")
                }
            }

            if (filterParts.isNotEmpty()) {
                append("-filter_complex \"${filterParts.joinToString(";")}\" ")
            }

            outVideos.forEach {
                append("-map \"$it\" ")
                append("-c:v mpeg4 ")
                append("-pix_fmt yuv420p -r ${frameRate.value} ")
            }

            outAudios.forEach {
                append("-map \"$it\" ")
                append("-c:a aac -ar 44100 -ac 2 ")
            }

            append("\"${output.getPath(context, "mp4")}\"")
        }

        printLog("Merge command: $command")
        return command
    }

    private fun convertVideoInput(
        videosInfo: List<TruvideoSdkVideoInformation>,
        data: List<TruvideoSdkVideoMergeVideoTrack>,
        width: Int?,
        height: Int?
    ): List<VideoTrack> {
        val result = mutableListOf<VideoTrack>()

        fun calculateMaxSize(
            widths: List<Int>,
            heights: List<Int>,
            streamMaxWidth: Int? = null,
            streamMaxHeight: Int? = null
        ): Size {
            val maxWidth: Int
            val maxHeight: Int
            if (streamMaxWidth != null && streamMaxHeight != null) {
                maxWidth = streamMaxWidth
                maxHeight = streamMaxHeight
            } else {
                val currentMaxWidth = widths.maxOrNull()
                val currentMaxHeight = heights.maxOrNull()

                if (currentMaxWidth == null || currentMaxHeight == null) {
                    throw TruvideoSdkException("None of the videos from the video track contains width or height")
                }

                if (streamMaxWidth != null) {
                    maxWidth = streamMaxWidth
                    maxHeight = (streamMaxWidth * currentMaxHeight / currentMaxWidth)
                } else if (streamMaxHeight != null) {
                    maxWidth = (streamMaxHeight * currentMaxWidth / currentMaxHeight)
                    maxHeight = streamMaxHeight
                } else {
                    maxWidth = currentMaxWidth
                    maxHeight = currentMaxHeight
                }
            }

            return Size(maxWidth, maxHeight)
        }

        if (data.isEmpty()) {
            val maxVideoCount = videosInfo.maxOfOrNull { it.videoTracks.size } ?: 0
            for (videoTrackIndex in 0 until maxVideoCount) {
                val tracks = mutableListOf<VideoItem>()
                videosInfo.forEach { videoInfo ->
                    tracks.add(
                        VideoItem(
                            videoInfo = videoInfo,
                            trackInfo = videoInfo.videoTracks.elementAtOrNull(videoTrackIndex)
                        )
                    )
                }

                if (tracks.isNotEmpty()) {
                    val size = calculateMaxSize(
                        widths = tracks.mapNotNull { it.trackInfo?.rotatedWidth }.toList(),
                        heights = tracks.mapNotNull { it.trackInfo?.rotatedHeight }.toList(),
                        streamMaxWidth = width,
                        streamMaxHeight = height
                    )

                    result.add(
                        VideoTrack(
                            width = size.width,
                            height = size.height,
                            tracks = tracks
                        )
                    )
                }
            }
        } else {
            data.forEach { item ->
                val tracks = mutableListOf<VideoItem>()
                item.tracks.forEach { track ->
                    val videoInfo = videosInfo.elementAtOrNull(track.fileIndex)
                    if (videoInfo != null) {
                        val trackInfo = videoInfo.videoTracks.firstOrNull { it.index == track.entryIndex }
                        tracks.add(
                            VideoItem(
                                videoInfo = videoInfo,
                                trackInfo = trackInfo
                            )
                        )
                    }
                }

                if (tracks.isNotEmpty()) {
                    val size = calculateMaxSize(
                        widths = tracks.mapNotNull { it.trackInfo?.rotatedWidth }.toList(),
                        heights = tracks.mapNotNull { it.trackInfo?.rotatedHeight }.toList(),
                        streamMaxWidth = item.width ?: width,
                        streamMaxHeight = item.height ?: height
                    )

                    result.add(
                        VideoTrack(
                            width = size.width,
                            height = size.height,
                            tracks = tracks,
                        )
                    )
                }
            }
        }

        return result.toList()
    }

    private fun convertAudioInput(
        videosInfo: List<TruvideoSdkVideoInformation>,
        data: List<TruvideoSdkVideoMergeAudioTrack>
    ): List<AudioTrack> {
        val result = mutableListOf<AudioTrack>()

        if (data.isEmpty()) {
            val maxCount = videosInfo.maxOfOrNull { it.audioTracks.size } ?: 0
            for (i in 0 until maxCount) {
                val tracks = mutableListOf<AudioItem>()
                videosInfo.forEach { videoInfo ->
                    tracks.add(
                        AudioItem(
                            videoInfo = videoInfo,
                            trackInfo = videoInfo.audioTracks.elementAtOrNull(i)
                        )
                    )
                }

                if (tracks.isNotEmpty()) {
                    result.add(
                        AudioTrack(tracks = tracks)
                    )
                }
            }

        } else {
            data.forEach { item ->
                val tracks = mutableListOf<AudioItem>()
                item.tracks.forEach { track ->
                    val videoInfo = videosInfo.elementAtOrNull(track.fileIndex)
                    if (videoInfo != null) {
                        val trackInfo = videoInfo.audioTracks.firstOrNull { it.index == track.entryIndex }
                        tracks.add(
                            AudioItem(
                                videoInfo = videoInfo,
                                trackInfo = trackInfo
                            )
                        )
                    }
                }

                if (tracks.isNotEmpty()) {
                    result.add(
                        AudioTrack(tracks = tracks)
                    )
                }
            }
        }

        return result.toList()
    }
}

