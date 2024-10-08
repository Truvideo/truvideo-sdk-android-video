package com.truvideo.sdk.video.usecases

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.truvideo.sdk.video.FFmpegTest
import com.truvideo.sdk.video.FileUtils
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoFFmpegAdapterImpl
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoMediaManager
import com.truvideo.sdk.video.managers.TruvideoSdkVideoMediaManagerImpl
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class ClearVideoAudioNoiseUseCaseTest {

    private lateinit var fFmpegAdapter: TruvideoSdkVideoFFmpegAdapter
    private lateinit var mediaManager: TruvideoSdkVideoMediaManager

    private lateinit var appContext: Context
    private lateinit var cacheDir: File

    @Before
    fun before() {
        fFmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()
        mediaManager = TruvideoSdkVideoMediaManagerImpl(fFmpegAdapter)

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    }

    @Test(expected = Exception::class)
    fun ffmpegMergeAudioWithVideoInvalidPath(): Unit = runBlocking {
        mediaManager.mergeAudioWithVideo("", "", "")
    }

    @Test(expected = Exception::class)
    fun ffmpegExtractAudioInvalidPath(): Unit = runBlocking {
        mediaManager.extractAudioFromVideo("", "")
    }

    @Test(expected = Exception::class)
    fun ffmpegExtractAudioInvalidAudioPath(): Unit = runBlocking {
        val fileIdentifier = appContext.resources.getIdentifier(
            FFmpegTest.MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
        )
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val file = File(cacheDir, FFmpegTest.NEW_VIDEO_FILE_NAME)
        FileUtils.copyInputStreamToFile(iS, file)
        mediaManager.extractAudioFromVideo(file.absolutePath, "")
    }

    @Test(expected = Exception::class)
    fun ffmpegMergeAudioAndVideoAudioInvalidPath(): Unit = runBlocking {
        val fileIdentifier = appContext.resources.getIdentifier(
            FFmpegTest.MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
        )
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val file = File(cacheDir, FFmpegTest.NEW_VIDEO_FILE_NAME)
        val destinationFile = File(cacheDir, FFmpegTest.DESTINATION_VIDEO_FILE_NAME)
        FileUtils.copyInputStreamToFile(iS, file)
        mediaManager.mergeAudioWithVideo(file.absolutePath, "", destinationFile.absolutePath)
    }

    @Test
    fun ffmpegExtractAudioValidPath(): Unit = runBlocking {
        val fileIdentifier = appContext.resources.getIdentifier(
            FFmpegTest.MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
        )
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val file = File(cacheDir, FFmpegTest.NEW_VIDEO_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }

        val wavFile = File(cacheDir, FFmpegTest.OUTPUT_WAV_FILE_NAME)
        if (wavFile.exists()) {
            wavFile.delete()
        }

        FileUtils.copyInputStreamToFile(iS, file)
        mediaManager.extractAudioFromVideo(file.absolutePath, wavFile.absolutePath)

        wavFile.delete()
        file.delete()
    }

    @Test
    fun ffmpegMergeAudioAndVideo(): Unit = runBlocking {
        val videoFileIdentifier = appContext.resources.getIdentifier(
            FFmpegTest.MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
        )
        val videoIS = appContext.resources.openRawResource(videoFileIdentifier)
        val videoFile = File(cacheDir, FFmpegTest.NEW_VIDEO_FILE_NAME)
        if (videoFile.exists()) {
            videoFile.delete()
        }
        FileUtils.copyInputStreamToFile(videoIS, videoFile)

        val wavFileIdentifier = appContext.resources.getIdentifier(
            FFmpegTest.WAV_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
        )
        val wavIS = appContext.resources.openRawResource(wavFileIdentifier)
        val wavFile = File(cacheDir, FFmpegTest.NEW_VIDEO_AUDIO_FILE_NAME)
        if (wavFile.exists()) {
            wavFile.delete()
        }

        FileUtils.copyInputStreamToFile(wavIS, wavFile)

        val destinationVideoFile = File(cacheDir, FFmpegTest.DESTINATION_VIDEO_FILE_NAME)
        if (destinationVideoFile.exists()) {
            destinationVideoFile.delete()
        }

        mediaManager.mergeAudioWithVideo(
            videoFile.absolutePath, wavFile.absolutePath, destinationVideoFile.absolutePath
        )

        wavFile.delete()
        videoFile.delete()
        destinationVideoFile.delete()
    }

    @Test
    fun ffmpegRemoveAudioFromVideo(): Unit = runBlocking {
        val fileIdentifier = appContext.resources.getIdentifier(
            FFmpegTest.MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
        )
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val file = File(cacheDir, FFmpegTest.NEW_VIDEO_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }

        val wavFile = File(cacheDir, FFmpegTest.OUTPUT_WAV_FILE_NAME)
        if (wavFile.exists()) {
            wavFile.delete()
        }

        FileUtils.copyInputStreamToFile(iS, file)
        mediaManager.extractAudioFromVideo(file.absolutePath, wavFile.absolutePath)

        wavFile.delete()
        file.delete()
    }

}