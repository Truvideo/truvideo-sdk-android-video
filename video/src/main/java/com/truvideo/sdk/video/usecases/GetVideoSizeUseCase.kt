package com.truvideo.sdk.video.usecases

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File
import kotlin.coroutines.suspendCoroutine

internal class GetVideoSizeUseCase(
    private val context: Context
) {

    suspend operator fun invoke(videoPath: String): Pair<Int, Int> = suspendCoroutine {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(context, Uri.fromFile(File(videoPath)))
            mediaPlayer.prepare()
            val width = mediaPlayer.videoWidth
            val height = mediaPlayer.videoHeight
            mediaPlayer.release()
            it.resumeWith(Result.success(Pair(width, height)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            it.resumeWith(Result.failure(ex))
        }
    }
}