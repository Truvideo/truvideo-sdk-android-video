package com.truvideo.sdk.video.interfaces

import android.content.Context
import com.truvideo.sdk.video.model.AudioFormat
import com.truvideo.sdk.video.model.BitRate
import java.io.File

internal interface TruvideoSdkVideoMediaManager {

    suspend fun extractAudioFromVideo(
        videoPath: String,
        outputWavPath: String,
        format: AudioFormat = AudioFormat.Wav,
        audioChannels: Int = 1,
        samplingRate: Int = 16_000, // how many samples taken to represent a second of audio,
        bitRate: BitRate = BitRate.Regular,
    ): File

    suspend fun mergeAudioWithVideo(
        videoPath: String,
        audioPath: String,
        resultPath: String
    ): File

    suspend fun clearNoiseFromAudio(
        context: Context,
        audioPath: String
    ): ByteArray

    suspend fun rotateVideo(
        context: Context,
        videoPath: String,
        videoOutputPath: String
    ): File

    suspend fun removeAudioTrack(
        context: Context,
        videoPath: String,
        videoOutputPath: String
    ): File
}