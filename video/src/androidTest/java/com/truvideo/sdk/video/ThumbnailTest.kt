package com.truvideo.sdk.video

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.truvideo.sdk.video.usecases.CreateVideoThumbnailUseCase
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ThumbnailTest {

    companion object {
        const val MP4_TEST_FILE_IDENTIFIER = "test_video"
        const val MP4_TEST_FILE = "test_video_input_file.mp4"
        const val DESTINATION_VIDEO_FILE_NAME = "test_video_output_file_thumbnail.png"
    }

    private lateinit var createVideoThumbnail: CreateVideoThumbnailUseCase
/*
    @Before
    fun before() {
        createVideoThumbnail = CreateVideoThumbnailUseCase(FFmpegAdapterImpl())
    }

    @Test
    fun testVideoThumbnailCreation() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File(cacheDir, MP4_TEST_FILE)
        FileUtils.copyInputStreamToFile(iS, file)

        val destinationThumbnailFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
        if (destinationThumbnailFile.exists()) {
            destinationThumbnailFile.delete()
        }

        createVideoThumbnail(file.absolutePath, destinationThumbnailFile.absolutePath)

        val newFile = File(destinationThumbnailFile.absolutePath);
        val exists = newFile.exists()
        if (exists) newFile.delete()
        assert(exists)
    }

    @Test
    fun testVideoThumbnailCreationWidthCustomWidth() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File(cacheDir, MP4_TEST_FILE)
        FileUtils.copyInputStreamToFile(iS, file)

        val destinationThumbnailFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
        if (destinationThumbnailFile.exists()) {
            destinationThumbnailFile.delete()
        }

        val width = 200
        createVideoThumbnail(
            file.absolutePath,
            destinationThumbnailFile.absolutePath,
            width = width
        )
//        val imageResolution = ImageUtils.getImageResolution(destinationThumbnailFile.absolutePath)
//        println("Image resolution ${imageResolution[0]}x${imageResolution[1]}")
//        assert(imageResolution[0] == width)

        val newFile = File(destinationThumbnailFile.absolutePath)
        val exists = newFile.exists()
        if (exists) newFile.delete()
        assert(exists)
    }

    @Test
    fun testVideoThumbnailCreationWidthCustomInvalidWidth() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File(cacheDir, MP4_TEST_FILE)
        FileUtils.copyInputStreamToFile(iS, file)

        val destinationThumbnailFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
        if (destinationThumbnailFile.exists()) {
            destinationThumbnailFile.delete()
        }

        val size = -1
        createVideoThumbnail(
            file.absolutePath,
            destinationThumbnailFile.absolutePath,
            width = size
        )

        val newFile = File(destinationThumbnailFile.absolutePath);
        val exists = newFile.exists()
        if (exists) newFile.delete()

        assert(exists)
    }

    @Test
    fun testVideoThumbnailCreationWidthCustomHeight() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File(cacheDir, MP4_TEST_FILE)
        FileUtils.copyInputStreamToFile(iS, file)

        val destinationThumbnailFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
        if (destinationThumbnailFile.exists()) {
            destinationThumbnailFile.delete()
        }

        val height = 200
        createVideoThumbnail(
            file.absolutePath,
            destinationThumbnailFile.absolutePath,
            height = height
        )

//        val imageResolution = ImageUtils.getImageResolution(destinationThumbnailFile.absolutePath)
//        println("Image resolution ${imageResolution[0]}x${imageResolution[1]}")
//        assert(imageResolution[1] == height)

        val newFile = File(destinationThumbnailFile.absolutePath)
        val exists = newFile.exists()
        if (exists) newFile.delete()
        assert(exists)

    }

    @Test
    fun testVideoThumbnailCreationWidthCustomInvalidHeight() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File(cacheDir, MP4_TEST_FILE)
        FileUtils.copyInputStreamToFile(iS, file)

        val destinationThumbnailFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
        if (destinationThumbnailFile.exists()) {
            destinationThumbnailFile.delete()
        }

        createVideoThumbnail(
            file.absolutePath,
            destinationThumbnailFile.absolutePath,
            height = -1
        )

        val newFile = File(destinationThumbnailFile.absolutePath);
        val exists = newFile.exists()
        if (exists) newFile.delete()
        assert(exists)
    }

    @Test
    fun testVideoThumbnailCreationWidthCustomSize() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File(cacheDir, MP4_TEST_FILE)
        FileUtils.copyInputStreamToFile(iS, file)

        val destinationThumbnailFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
        if (destinationThumbnailFile.exists()) {
            destinationThumbnailFile.delete()
        }

        val width = 200
        val height = 200
        createVideoThumbnail(
            file.absolutePath,
            destinationThumbnailFile.absolutePath,
            width = width,
            height = height
        )
//
//        val imageResolution = ImageUtils.getImageResolution(destinationThumbnailFile.absolutePath)
//        println("Image resolution ${imageResolution[0]}x${imageResolution[1]}")
//        assert(imageResolution[0] == width)
//        assert(imageResolution[1] == height)

        val newFile = File(destinationThumbnailFile.absolutePath);
        val exists = newFile.exists()
        if (exists) newFile.delete()
        assert(exists)
    }

    @Test
    fun testVideoThumbnailCreationWidthCustomInvalidSize() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val fileIdentifier = appContext.resources.getIdentifier(MP4_TEST_FILE_IDENTIFIER, "raw", appContext.packageName)
        val iS = appContext.resources.openRawResource(fileIdentifier)
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File(cacheDir, MP4_TEST_FILE)
        FileUtils.copyInputStreamToFile(iS, file)

        val destinationThumbnailFile = File(cacheDir, DESTINATION_VIDEO_FILE_NAME)
        if (destinationThumbnailFile.exists()) {
            destinationThumbnailFile.delete()
        }

        createVideoThumbnail(
            file.absolutePath,
            destinationThumbnailFile.absolutePath,
            width = -1,
            height = -1
        )

        val newFile = File(destinationThumbnailFile.absolutePath);
        val exists = newFile.exists()
        if (exists) newFile.delete()
        assert(exists)
    }*/
}