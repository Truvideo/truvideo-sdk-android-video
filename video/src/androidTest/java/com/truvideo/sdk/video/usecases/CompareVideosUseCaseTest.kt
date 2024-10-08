package com.truvideo.sdk.video.usecases

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoFFmpegAdapterImpl
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoMediaManager
import com.truvideo.sdk.video.managers.TruvideoSdkVideoMediaManagerImpl
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class CompareVideosUseCaseTest {

    private lateinit var fFmpegAdapter: TruvideoSdkVideoFFmpegAdapter
    private lateinit var mediaManager: TruvideoSdkVideoMediaManager

    @Before
    fun before() {
        fFmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()
        mediaManager = TruvideoSdkVideoMediaManagerImpl(fFmpegAdapter)
    }
}