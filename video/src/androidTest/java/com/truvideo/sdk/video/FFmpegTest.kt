package com.truvideo.sdk.video

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File

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

    @Before
    fun before() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    }

}