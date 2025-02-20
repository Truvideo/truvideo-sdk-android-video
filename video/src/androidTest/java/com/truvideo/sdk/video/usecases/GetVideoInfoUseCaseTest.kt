package com.truvideo.sdk.video.usecases

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoFFmpegAdapterImpl
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoMediaManager
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class GetVideoInfoUseCaseTest {

    private lateinit var ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
    private lateinit var mediaManager: TruvideoSdkVideoMediaManager

    private lateinit var appContext: Context
    private lateinit var cacheDir: File

    @Before
    fun before() {
        ffmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    }



    companion object {
        private const val TAG = "GetVideoInfoUseCaseTest"
    }
}