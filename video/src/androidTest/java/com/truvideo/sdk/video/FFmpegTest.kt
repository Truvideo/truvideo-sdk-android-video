//package com.truvideo.sdk.video
//
//import android.content.Context
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.filters.SmallTest
//import androidx.test.platform.app.InstrumentationRegistry
//import kotlinx.coroutines.runBlocking
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import truvideo.sdk.common.exception.TruvideoSdkException
//import java.io.File
//
//@RunWith(AndroidJUnit4::class)
//@SmallTest
//class FFmpegTest {
//
//    companion object {
//        const val MP4_TEST_FILE_IDENTIFIER = "test_video"
//        const val WAV_TEST_FILE_IDENTIFIER = "test_video_audio"
//        const val NEW_VIDEO_FILE_NAME = "test_audio_file.mp4"
//        const val NEW_VIDEO_AUDIO_FILE_NAME = "test_video_audio_file.wav"
//        const val OUTPUT_WAV_FILE_NAME = "output.wav"
//        const val DESTINATION_VIDEO_FILE_NAME = "destination_test_audio_file.mp4"
//    }
//
//    private lateinit var appContext: Context
//    private lateinit var cacheDir: File
////    private lateinit var fFmpegAdapter: FFmpegAdapter
////    private lateinit var mediaManager: MediaManager
////    private lateinit var mediaManager: TruvideoSdkVideoMediaManager
//
//    @Before
//    fun before() {
//        appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
////        fFmpegAdapter = FFmpegAdapterImpl()
////        mediaManager = MediaManagerImpl(fFmpegAdapter)
//    }
//
//    @Test(expected = TruvideoSdkException::class)
//    fun ffmpegExtractAudioInvalidPath(): Unit = runBlocking {
////        mediaManager.extractAudioFromVideo("", "")
//    }
//
//    @Test(expected = TruvideoSdkException::class)
//    fun ffmpegMergeAudioWithVideoInvalidPath() : Unit = runBlocking {
////        mediaManager.mergeAudioWithVideo("", "", "")
//    }
//
//    @Test(expected = TruvideoSdkException::class)
//    fun ffmpegExtractAudioInvalidAudioPath(): Unit = runBlocking {
//        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val iS = appContext.resources.openRawResource(fileIdentifier)
//        val file = File(cacheDir, NEW_VIDEO_FILE_NAME)
//        FileUtils.copyInputStreamToFile(iS, file)
//        mediaManager.extractAudioFromVideo(file.absolutePath, "")
//    }
//
//    @Test(expected = TruvideoSdkException::class)
//    fun ffmpegMergeAudioAndVideoAudioInvalidPath(): Unit = runBlocking {
//        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val iS = appContext.resources.openRawResource(fileIdentifier)
//        val file = File(cacheDir, NEW_VIDEO_FILE_NAME)
//        val destinationFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
//        FileUtils.copyInputStreamToFile(iS, file)
//        mediaManager.mergeAudioWithVideo(file.absolutePath, "", destinationFile.absolutePath)
//    }
//
//    @Test
//    fun ffmpegExtractAudioValidPath(): Unit = runBlocking {
//        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val iS = appContext.resources.openRawResource(fileIdentifier)
//        val file = File(cacheDir, NEW_VIDEO_FILE_NAME)
//        if (file.exists()) {
//            file.delete()
//        }
//
//        val wavFile = File(cacheDir, OUTPUT_WAV_FILE_NAME)
//        if (wavFile.exists()) {
//            wavFile.delete()
//        }
//
//        FileUtils.copyInputStreamToFile(iS, file)
//        mediaManager.extractAudioFromVideo(file.absolutePath, wavFile.absolutePath)
//
//        wavFile.delete()
//        file.delete()
//    }
//
//    @Test
//    fun ffmpegMergeAudioAndVideo(): Unit = runBlocking {
//        val videoFileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val videoIS = appContext.resources.openRawResource(videoFileIdentifier)
//        val videoFile = File(cacheDir, NEW_VIDEO_FILE_NAME)
//        if (videoFile.exists()) {
//            videoFile.delete()
//        }
//        FileUtils.copyInputStreamToFile(videoIS, videoFile)
//
//        val wavFileIdentifier = appContext.resources.getIdentifier(WAV_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val wavIS = appContext.resources.openRawResource(wavFileIdentifier)
//        val wavFile = File(cacheDir, NEW_VIDEO_AUDIO_FILE_NAME)
//        if (wavFile.exists()) {
//            wavFile.delete()
//        }
//
//        FileUtils.copyInputStreamToFile(wavIS, wavFile)
//
//        val destinationVideoFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
//        if (destinationVideoFile.exists()) {
//            destinationVideoFile.delete()
//        }
//
//        mediaManager.mergeAudioWithVideo(videoFile.absolutePath, wavFile.absolutePath, destinationVideoFile.absolutePath)
//
//        wavFile.delete()
//        videoFile.delete()
//        destinationVideoFile.delete()
//    }
//
//    @Test
//    fun ffmpegRemoveAudioFromVideo(): Unit = runBlocking {
//        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val iS = appContext.resources.openRawResource(fileIdentifier)
//        val file = File(cacheDir, NEW_VIDEO_FILE_NAME)
//        if (file.exists()) {
//            file.delete()
//        }
//
//        val wavFile = File(cacheDir, OUTPUT_WAV_FILE_NAME)
//        if (wavFile.exists()) {
//            wavFile.delete()
//        }
//
//        FileUtils.copyInputStreamToFile(iS, file)
//        mediaManager.extractAudioFromVideo(file.absolutePath, wavFile.absolutePath)
//
//        wavFile.delete()
//        file.delete()
//    }
//
//
//    @Test
//    fun ffmpegRotateVideo(): Unit = runBlocking {
//        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val iS = appContext.resources.openRawResource(fileIdentifier)
//        val file = File(cacheDir, NEW_VIDEO_FILE_NAME)
//        if (file.exists()) {
//            file.delete()
//        }
//
//        FileUtils.copyInputStreamToFile(iS, file)
//
//        val destinationVideoFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
//        if (destinationVideoFile.exists()) {
//            destinationVideoFile.delete()
//        }
//
//        mediaManager.rotateVideo(appContext, file.absolutePath, destinationVideoFile.absolutePath)
//        assert(destinationVideoFile.exists())
//
//        file.delete()
//
////        file.delete()
//    }
//
//    @Test
//    fun ffmpegRemoveVideoAudio(): Unit = runBlocking {
//        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val iS = appContext.resources.openRawResource(fileIdentifier)
//        val file = File(cacheDir, NEW_VIDEO_FILE_NAME)
//        if (file.exists()) {
//            file.delete()
//        }
//
//        FileUtils.copyInputStreamToFile(iS, file)
//
//        val destinationVideoFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
//        if (destinationVideoFile.exists()) {
//            destinationVideoFile.delete()
//        }
//
//        mediaManager.removeAudioTrack(appContext, file.absolutePath, destinationVideoFile.absolutePath)
//        assert(destinationVideoFile.exists())
//
//        file.delete()
//
////        file.delete()
//    }
//
//}