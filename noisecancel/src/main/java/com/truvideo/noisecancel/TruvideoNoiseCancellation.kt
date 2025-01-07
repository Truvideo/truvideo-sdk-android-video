package com.truvideo.noisecancel

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TruvideoNoiseCancellation {

    private val mModelName = "model_32.kw"
    private val channelMask = 16
    private val sampleRate = 32000
    private val encoding = 2
    private val mutex = Mutex()

    init {
        System.loadLibrary("native-lib")
    }

    suspend fun call(
        context: Context,
        inputPath: String,
        outputPath: String,
    ): String {
        return mutex.withLock {
            return@withLock withContext(Dispatchers.IO) {
                try {
                    val mediaDirectory = FileUtils.getMediaDirectory(context) ?: throw Exception("Media directory not found")
                    NoiseCancelNative.globalInit(mediaDirectory.path)

                    // Load model
                    val model = ModelUtils.load(context, mModelName)
                    NoiseCancelNative.setModel(model.absolutePath, mModelName)

                    // Create session
                    NoiseCancelNative.createSession(mModelName)

                    // Run
                    clearAudio(
                        inputPath = inputPath,
                        outputPath = outputPath
                    )
                    return@withContext outputPath
                } catch (exception: Exception) {
                    Log.d("TruvideoSdkVideo", "[NC] Error processing", exception)
                    exception.printStackTrace()
                    throw exception
                } finally {
                    // Clear Krisp
                    close()
                }
            }
        }
    }

    private fun clearAudio(
        inputPath: String,
        outputPath: String
    ): String {
        try {
            val inputFile = File(inputPath)
            if (!inputFile.exists()) {
                throw Exception("Input file not found")
            }

            val outputFile = File(outputPath)
            if (outputFile.exists()) outputFile.delete()

            val fileSize = inputFile.length()
            val chunkSize = minOf(fileSize.toInt(), 1024 * 512)
            Log.d("TruvideoSdkVideo", "[NC] Chunk Size: $chunkSize. File size: $fileSize")

            FileInputStream(inputFile).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    createWavHeader(outputStream, channelMask, sampleRate, encoding)

                    val buffer = ByteArray(chunkSize)
                    var bytesRead: Int

                    var index = 0
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        Log.d("TruvideoSdkVideo", "[NC] chunk: $index. bytes: $bytesRead")
                        val processedChunk = NoiseCancelNative.processNCChunk(buffer, bytesRead)
                        outputStream.write(processedChunk)

                        index++
                    }
                }
            }
            updateWavHeader(outputFile)
            return outputPath
        } catch (exception: Exception) {
            Log.d("TruvideoSdkVideo", "[NC] Error clearing audio", exception)
            exception.printStackTrace()
            throw exception
        }
    }

    private fun close() {
        NoiseCancelNative.closeSession()
        NoiseCancelNative.globalDestroy()
    }

    @Throws(IOException::class)
    private fun createWavHeader(out: OutputStream, channelMask: Int, sampleRate: Int, encoding: Int) {
        val channels: Byte = when (channelMask) {
            12 -> 2
            16 -> 1
            else -> throw IllegalArgumentException("Unacceptable channel mask")
        }

        val bitDepth: Byte = when (encoding) {
            2 -> 16
            3 -> 8
            4 -> 32
            else -> throw IllegalArgumentException("Unacceptable encoding")
        }

        val byteRate = sampleRate * channels * (bitDepth / 8)
        val blockAlign = (channels * (bitDepth / 8)).toShort()

        val headerBuffer = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        headerBuffer.put("RIFF".toByteArray(Charsets.US_ASCII))     // "RIFF"
        headerBuffer.putInt(0)                                // Chunk Size
        headerBuffer.put("WAVE".toByteArray(Charsets.US_ASCII))     // "WAVE"
        headerBuffer.put("fmt ".toByteArray(Charsets.US_ASCII))     // "fmt "
        headerBuffer.putInt(16)                               // Subchunk1Size (PCM)
        headerBuffer.putShort(1)                              // Audio format (1 = PCM)
        headerBuffer.putShort(channels.toShort())                   // Number of channels
        headerBuffer.putInt(sampleRate)                             // Sample rate
        headerBuffer.putInt(byteRate)                               // Byte rate
        headerBuffer.putShort(blockAlign)                           // Block align
        headerBuffer.putShort(bitDepth.toShort())                   // Bits per sample
        headerBuffer.put("data".toByteArray(Charsets.US_ASCII))     // "data"
        headerBuffer.putInt(0)                                // Subchunk2Size

        out.write(headerBuffer.array())
    }

    private fun updateWavHeader(file: File) {
        val sizes = ByteBuffer.allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt((file.length() - 8L).toInt())
            .putInt((file.length() - 44L).toInt())
            .array()

        var accessWave: RandomAccessFile? = null

        try {
            accessWave = RandomAccessFile(file, "rw")
            accessWave.seek(4L)
            accessWave.write(sizes, 0, 4)
            accessWave.seek(40L)
            accessWave.write(sizes, 4, 4)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            accessWave?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}