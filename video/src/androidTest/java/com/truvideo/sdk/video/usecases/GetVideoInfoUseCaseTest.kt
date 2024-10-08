package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.truvideo.sdk.video.FFmpegTest
import com.truvideo.sdk.video.FileUtils
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoFFmpegAdapterImpl
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoMediaManager
import com.truvideo.sdk.video.managers.TruvideoSdkVideoMediaManagerImpl
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class GetVideoInfoUseCaseTest {

    private lateinit var ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
    private lateinit var mediaManager: TruvideoSdkVideoMediaManager

    private lateinit var appContext: Context
    private lateinit var cacheDir: File

    @Before
    fun before() {
        ffmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()
        mediaManager = TruvideoSdkVideoMediaManagerImpl(ffmpegAdapter)

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    }

    @Test(expected = Exception::class)
    fun ffmpegGetInformationInvalidPath(): Unit = runBlocking {
        ffmpegAdapter.getInformation("")
    }

    @Test
    fun ffmpegGetInformationValidPath(): Unit = runBlocking {
        val fileIdentifier = appContext.resources.getIdentifier(
            FFmpegTest.MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName
        )
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val file = File(cacheDir, FFmpegTest.NEW_VIDEO_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }

        FileUtils.copyInputStreamToFile(iS, file)

        val videoPath = file.absolutePath
        val information = ffmpegAdapter.getInformation(videoPath)

        file.delete()

        Log.d(TAG, "Information: $information")
        val videoSize = Pair(information.width, information.height)
        val videoWidth = videoSize.first
        val videoHeight = videoSize.second

        if (videoWidth == 0) {
            throw TruvideoSdkVideoException("Invalid video width")
        }

        if (videoHeight == 0) {
            throw TruvideoSdkVideoException("Invalid video height")
        }

        val truvideoSdkVideoInformation = TruvideoSdkVideoInformation(
            path = videoPath,
            durationMillis = information.durationMillis,
            width = videoWidth,
            height = videoHeight,
            size = information.size,
            withVideo = information.withVideo,
            videoCodec = information.videoCodec,
            videoPixelFormat = information.videoPixelFormat,
            withAudio = information.withAudio,
            audioCodec = information.audioCodec,
            audioSampleRate = information.audioSampleRate,
            rotation = information.rotation
        )

        assert(truvideoSdkVideoInformation.width == 3840)
        assert(truvideoSdkVideoInformation.height == 2160)
    }

    companion object {
        private const val TAG = "GetVideoInfoUseCaseTest"
    }
}