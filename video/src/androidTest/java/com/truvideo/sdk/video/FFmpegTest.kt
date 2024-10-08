package com.truvideo.sdk.video

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
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
@SmallTest
class FFmpegTest {

    companion object {
        const val MP4_TEST_FILE_IDENTIFIER = "test_video"
        const val WAV_TEST_FILE_IDENTIFIER = "test_video_audio"
        const val NEW_VIDEO_FILE_NAME = "test_audio_file.mp4"
        const val NEW_VIDEO_AUDIO_FILE_NAME = "test_video_audio_file.wav"
        const val OUTPUT_WAV_FILE_NAME = "output.wav"
        const val DESTINATION_VIDEO_FILE_NAME = "destination_test_audio_file.mp4"
    }

    private lateinit var appContext: Context
    private lateinit var cacheDir: File
    private lateinit var fFmpegAdapter: TruvideoSdkVideoFFmpegAdapter

    private lateinit var mediaManager: TruvideoSdkVideoMediaManager

    @Before
    fun before() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir

        fFmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()
        mediaManager = TruvideoSdkVideoMediaManagerImpl(fFmpegAdapter)
    }

//    @Test
//    fun ffmpegRotateVideo(): Unit = runBlocking {
//        val fileIdentifier = appContext.resources.getIdentifier(
//            MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
//        )
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

    @Test
    fun ffmpegRemoveVideoAudio(): Unit = runBlocking {
        val fileIdentifier = appContext.resources.getIdentifier(
            MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
        )
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val file = File(cacheDir, NEW_VIDEO_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }

        FileUtils.copyInputStreamToFile(iS, file)

        val destinationVideoFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
        if (destinationVideoFile.exists()) {
            destinationVideoFile.delete()
        }

        mediaManager.removeAudioTrack(
            appContext, file.absolutePath, destinationVideoFile.absolutePath
        )
        assert(destinationVideoFile.exists())

        file.delete()

//        file.delete()
    }

}