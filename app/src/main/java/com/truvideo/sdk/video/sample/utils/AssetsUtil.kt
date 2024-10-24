package com.truvideo.sdk.video.sample.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

object AssetsUtil {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun getFileFromAssets(
        context: Context,
        assetName: String,
        fileName: String
    ): File {
        return suspendCoroutine {
            scope.launch {
                val assetManager = context.assets
                val outFile = File(context.cacheDir, fileName)

                try {
                    val inputStream = assetManager.open(assetName)
                    val outputStream = FileOutputStream(outFile)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    it.resumeWith(Result.success(outFile))
                } catch (e: IOException) {
                    e.printStackTrace()
                    it.resumeWith(Result.failure(e))
                }
            }
        }
    }
}