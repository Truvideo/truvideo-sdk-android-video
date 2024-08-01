package com.truvideo.sdk.video

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    @JvmStatic
    fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        try {
            val outputStream = FileOutputStream(file,false)
            var read: Int
            val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
            read = inputStream.read(bytes)
            while (read != -1) {
                outputStream.write(bytes, 0, read)
                read = inputStream.read(bytes)
            }
        } catch ( e:Exception ){
            e.printStackTrace()
            throw e
        }
    }
}