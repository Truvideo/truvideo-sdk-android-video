package com.truvideo.noisecancel

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

internal object FileUtils {
    fun copyInputStreamToFile(inputStream: InputStream, outputFile: File): File {
        inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } > 0) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        }
        return outputFile
    }

    fun getMediaDirectory(context: Context): File? {
        val storageDir = File(context.filesDir.toString() + "/NoiseCancellation/")
        return if (!storageDir.mkdirs() && !storageDir.exists()) null else storageDir
    }
}