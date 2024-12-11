package com.truvideo.sdk.video.usecases

import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation

internal class CompareVideosUseCase(
    private val getVideoInfoUseCase: GetVideoInfoUseCase
) {
    suspend operator fun invoke(input: List<TruvideoSdkVideoFile>): Boolean {
        if (input.isEmpty()) return true

        val videoInfos: List<TruvideoSdkVideoInformation> = input.map { getVideoInfoUseCase(it) }.toList()
        val firstVideoInfo = videoInfos.first()

        for (videoInfo in videoInfos) {
            if (videoInfo.videoTracks.size != firstVideoInfo.videoTracks.size || videoInfo.audioTracks.size != firstVideoInfo.audioTracks.size) {
                return false
            }

            if (videoInfo.format != firstVideoInfo.format) return false

            for (i in videoInfo.videoTracks.indices) {
                val video1 = firstVideoInfo.videoTracks[i]
                val video2 = videoInfo.videoTracks[i]

                if (video1.width != video2.width) return false
                if (video1.rotatedWidth != video2.rotatedWidth) return false
                if (video1.height != video2.height) return false
                if (video1.rotatedHeight != video2.rotatedHeight) return false
                if (video1.bitrate != video2.bitrate) return false
                if (video1.codec != video2.codec) return false
                if (video1.codecTag != video2.codecTag) return false
                if (video1.frameRate != video2.frameRate) return false
                if (video1.pixelFormat != video2.pixelFormat) return false
                if (video1.rotation != video2.rotation) return false
            }

            for (i in videoInfo.audioTracks.indices) {
                val audio1 = firstVideoInfo.audioTracks[i]
                val audio2 = videoInfo.audioTracks[i]

                if (audio1.codec != audio2.codec) return false
                if (audio1.codecTag != audio2.codecTag) return false
                if (audio1.sampleFormat != audio2.sampleFormat) return false
                if (audio1.bitrate != audio2.bitrate) return false
                if (audio1.sampleRate != audio2.sampleRate) return false
                if (audio1.channels != audio2.channels) return false
                if (audio1.channelLayout != audio2.channelLayout) return false
            }
        }

        return true
    }

    companion object {
        private const val TAG = "CompareVideosUseCase"
    }
}