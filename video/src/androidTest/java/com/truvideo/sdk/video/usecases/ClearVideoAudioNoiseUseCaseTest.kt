package com.truvideo.sdk.video.usecases

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoFFmpegAdapterImpl
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ClearVideoAudioNoiseUseCaseTest {

    private lateinit var fFmpegAdapter: TruvideoSdkVideoFFmpegAdapter

    private lateinit var appContext: Context
    private lateinit var cacheDir: File

    @Before
    fun before() {
        fFmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    }
}