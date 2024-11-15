package com.truvideo.sdk.video.usecases

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoFFmpegAdapterImpl
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompareVideosUseCaseTest {

    private lateinit var fFmpegAdapter: TruvideoSdkVideoFFmpegAdapter

    @Before
    fun before() {
        fFmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()
    }
}