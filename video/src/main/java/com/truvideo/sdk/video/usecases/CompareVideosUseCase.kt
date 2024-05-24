package com.truvideo.sdk.video.usecases

import android.util.Log

internal class CompareVideosUseCase(
    private val getVideoInfoUseCase: GetVideoInfoUseCase
) {
    suspend operator fun invoke(videoPaths: List<String>): Boolean {
        val uniqueVideoCodecs = mutableSetOf<String?>()
        val uniqueVideoPixelFormats = mutableSetOf<String?>()
        val uniqueVideoWidth = mutableSetOf<Int>()
        val uniqueVideoHeight = mutableSetOf<Int>()
        val uniqueAudioCodecs = mutableSetOf<String?>()
        val uniqueAudioSampleRates = mutableSetOf<Int?>()

        for (videoPath in videoPaths) {
            getVideoInfoUseCase(videoPath).let {
                uniqueVideoCodecs.add(it.videoCodec)
                uniqueVideoPixelFormats.add(it.videoPixelFormat)
                uniqueVideoWidth.add(it.width)
                uniqueVideoHeight.add(it.height)
                uniqueAudioCodecs.add(it.audioCodec)
                uniqueAudioSampleRates.add(it.audioSampleRate)
            }
        }

        if (uniqueVideoCodecs.size > 1) {
            Log.d(TAG, "uniqueVideoCodecs")
            return false
        }

        if (uniqueVideoPixelFormats.size > 1) {
            Log.d(TAG, "uniqueVideoPixelFormats")
            return false
        }

        if (uniqueVideoWidth.size > 1) {
            Log.d(TAG, "uniqueVideoWidth")
            return false
        }

        if (uniqueVideoHeight.size > 1) {
            Log.d(TAG, "uniqueVideoHeight")
            return false
        }

        if (uniqueAudioCodecs.size > 1) {
            Log.d(TAG, "areVideosReadyToConcat: false uniqueAudioCodecs")
            return false
        }

        if (uniqueAudioSampleRates.size > 1) {
            Log.d(TAG, "areVideosReadyToConcat: false uniqueAudioSampleRates")
            return false
        }

        return true
    }

    companion object {
        private const val TAG = "CompareVideosUseCase"
    }
}