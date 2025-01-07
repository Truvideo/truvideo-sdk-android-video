package com.truvideo.noisecancel

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import kotlin.coroutines.suspendCoroutine

internal object ModelUtils {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val mutex = Mutex()

    suspend fun load(
        context: Context,
        name: String
    ): File {
        return suspendCoroutine { cont ->
            scope.launch {
                mutex.withLock {
                    try {
                        val mediaDirectory = FileUtils.getMediaDirectory(context) ?: throw Exception("Media directory not found")
                        val outputFile = File(mediaDirectory, name)

                        if (outputFile.exists()) {
                            Log.d("TruvideoSdkVideo", "[NC] Model $name already exists")
                            cont.resumeWith(Result.success(outputFile))
                            return@launch
                        }

                        Log.d("TruvideoSdkVideo", "[NC] Loading model $name")
                        val inputStream = context.assets.open(name)
                        val result = FileUtils.copyInputStreamToFile(inputStream, outputFile)
                        Log.d("TruvideoSdkVideo", "[NC] Model $name loaded")
                        cont.resumeWith(Result.success(result))
                    } catch (exception: Exception) {
                        Log.d("TruvideoSdkVideo", "[NC] Error loading model $name", exception)
                        exception.printStackTrace()
                        cont.resumeWith(Result.failure(exception))
                    }
                }
            }
        }
    }
}