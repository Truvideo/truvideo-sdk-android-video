package com.truvideo.sdk.video.usecases

import com.truvideo.sdk.video.model.TruvideoSdkVideoFrameRate
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoVideoCodec
import truvideo.sdk.common.exception.TruvideoSdkException
import kotlin.math.min

internal class GenerateMergeVideosCommandUseCase(
    private val getVideoInfoUseCase: GetVideoInfoUseCase,
) {
    suspend operator fun invoke(
        videoPaths: List<String>,
        resultPath: String,
        receivedWidth: Int?,
        receivedHeight: Int?,
        videoCodec: TruvideoSdkVideoVideoCodec,
        framesRate: TruvideoSdkVideoFrameRate
    ): String {
        if (receivedWidth != null && receivedWidth < 128) {
            throw TruvideoSdkException("Width must be 0 or greater than or equal to 128")
        }

        if (receivedHeight != null && receivedHeight < 128) {
            throw TruvideoSdkException("Height must be 0 or greater than or equal to 128")
        }

        var maxWidth = 0
        var maxHeight = 0

        val videosInfo = mutableListOf<TruvideoSdkVideoInformation>()
        for (videoPath in videoPaths) {
            val info: TruvideoSdkVideoInformation = getVideoInfoUseCase(videoPath)
            videosInfo.add(info)

            if (receivedWidth != null) {
                maxWidth = receivedWidth
            } else {
                val width = info.width
                if (width > maxWidth) {
                    maxWidth = width
                }
            }

            if (receivedHeight != null) {
                maxHeight = receivedHeight
            } else {
                val height = info.height
                if (height > maxHeight) {
                    maxHeight = height
                }
            }
        }

        maxWidth = convertToEven(maxWidth)
        maxHeight = convertToEven(maxHeight)

        val inputVideoPaths = videoPaths.joinToString(separator = " ") { "-i $it" }
        var filterComplex = " -filter_complex \""
        var query = ""
        var scale = ""
        var videoNumber = 0
        var hasAudio = false

        for (videoInfo in videosInfo) {
            if (!hasAudio) {
                hasAudio = videoInfo.withAudio
            }

            // Scale
            scale =
                "$scale [$videoNumber:v]scale=w=$maxWidth:h=$maxHeight:force_original_aspect_ratio=decrease,"

            // Pad
            var finalWidth = videoInfo.width
            var finalHeight = videoInfo.height

            val widthFactor: Double = maxWidth / videoInfo.width.toDouble()
            val heightFactor: Double = maxHeight / videoInfo.height.toDouble()

            val factor: Double = min(widthFactor, heightFactor)

            if (factor > 0 && factor < 1) {
                finalWidth = (videoInfo.width * factor).toInt()
                finalHeight = (videoInfo.height * factor).toInt()
            }

            val padX = (maxWidth - finalWidth) / 2
            val padY = (maxHeight - finalHeight) / 2

            val pad = "pad=w=$maxWidth:h=$maxHeight:x=$padX:y=$padY[video$videoNumber];"

            scale += pad

            query = query + " [video$videoNumber]" + if (hasAudio) "[$videoNumber:a]" else ""
            videoNumber += 1
        }

        filterComplex =
            filterComplex + scale + query + " concat=n=$videoNumber:v=1" + (if (hasAudio) ":a=1" else "") + " [v]" + (if (hasAudio) "[a]" else "") + "\""
        val otherConfigs =
            " -map \"[v]\" " + (if (hasAudio) "-map \"[a]\"" else "") + " -vsync 2 -c:v ${videoCodec.value}${
                getTagFor(
                    videoCodec
                )
            } -c:a aac -preset superfast "
        return "-y " + inputVideoPaths + " -r ${framesRate.value}" + filterComplex + otherConfigs + resultPath
    }

    private fun convertToEven(number: Int): Int {
        if (number % 2 != 0) {
            return number + 1
        }
        return number
    }

    private fun getTagFor(videoCodec: TruvideoSdkVideoVideoCodec): String {
        return if (videoCodec == TruvideoSdkVideoVideoCodec.h265) {
            " -tag:v hvc1"
        } else {
            ""
        }
    }


}