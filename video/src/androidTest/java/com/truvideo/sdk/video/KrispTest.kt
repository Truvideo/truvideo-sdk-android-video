package com.truvideo.sdk.video

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class KrispTest {

    companion object {
        const val WAV_TEST_FILE_IDENTIFIER = "krisp_test_1"
        const val NEW_WAV_FILE_NAME = "krisp_test_file.wav"
        const val INVALID_PATH = "invalid_path"
    }

//    private lateinit var mediaManager: MediaManager

    @Before
    fun before() {
//        val fFmpegAdapter = FFmpegAdapterImpl()
//        mediaManager = MediaManagerImpl(fFmpegAdapter)
    }

//    @Test
//    fun krispValidPath() = runBlocking {
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        val fileIdentifier = appContext.resources.getIdentifier(WAV_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
//        val iS = appContext.resources.openRawResource(fileIdentifier)
//        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
//        val file = File(cacheDir, NEW_WAV_FILE_NAME)
//        FileUtils.copyInputStreamToFile(iS, file)
//
//        val byteArray = mediaManager.clearNoiseFromAudio(appContext, file.absolutePath)
//        assert(byteArray != null)
//    }
//
//    @Test
//    fun krispInvalidPath() = runBlocking {
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        val result = mediaManager.clearNoiseFromAudio(appContext, INVALID_PATH)
//        assert(result == null)
//    }

}