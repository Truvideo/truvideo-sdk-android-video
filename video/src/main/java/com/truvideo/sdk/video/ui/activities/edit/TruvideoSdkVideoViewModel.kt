package com.truvideo.sdk.video.ui.activities.edit

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoFFmpegAdapterImpl
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import com.truvideo.sdk.video.model.degrees
import com.truvideo.sdk.video.usecases.ClearNoiseUseCase
import com.truvideo.sdk.video.usecases.CreateVideoThumbnailUseCase
import com.truvideo.sdk.video.usecases.EditVideoUseCase
import com.truvideo.sdk.video.usecases.ExtractAudioUseCase
import com.truvideo.sdk.video.usecases.GetVideoInfoUseCase
import com.truvideo.sdk.video.usecases.ReplaceAudioTrackUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class TruvideoSdkVideoViewModel(
    private val context: Context,
    private val input: TruvideoSdkVideoFile,
    private val output: TruvideoSdkVideoFileDescriptor,
    val previewMode: Boolean = false
) : ViewModel() {

    private lateinit var ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
    private lateinit var clearVideoAudioNoiseUseCase: ClearNoiseUseCase
    private lateinit var createVideoThumbnailUseCase: CreateVideoThumbnailUseCase
    private lateinit var extractAudioUseCase: ExtractAudioUseCase
    private lateinit var replaceAudioTrackUseCase: ReplaceAudioTrackUseCase
    private lateinit var getVideoInfoUseCase: GetVideoInfoUseCase
    private lateinit var editVideoUseCase: EditVideoUseCase
    private lateinit var ncFileDescription: TruvideoSdkVideoFileDescriptor
    private lateinit var thumbnailFileDescription: TruvideoSdkVideoFileDescriptor

    val previewItemCount: Int = 10

    init {
        if (!previewMode) {
            ffmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()

            extractAudioUseCase = ExtractAudioUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter
            )
            replaceAudioTrackUseCase = ReplaceAudioTrackUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter
            )
            clearVideoAudioNoiseUseCase = ClearNoiseUseCase(
                context = context,
                extractAudioUseCase = extractAudioUseCase,
                replaceAudioTrackUseCase = replaceAudioTrackUseCase
            )
            createVideoThumbnailUseCase = CreateVideoThumbnailUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter
            )
            getVideoInfoUseCase = GetVideoInfoUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter
            )
            editVideoUseCase = EditVideoUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter,
                getVideoInfoUseCase = getVideoInfoUseCase
            )

            ncFileDescription = TruvideoSdkVideoFileDescriptor.cache("video_nc")
            thumbnailFileDescription = TruvideoSdkVideoFileDescriptor.cache("video_thumbnail")
        }
    }

    private val _start: MutableStateFlow<Long> = MutableStateFlow(0L)
    private val _end: MutableStateFlow<Long> = MutableStateFlow(0L)
    private val _videoInfo: MutableStateFlow<TruvideoSdkVideoInformation?> = MutableStateFlow(null)
    private val _volume: MutableStateFlow<Float> = MutableStateFlow(1f)
    private val _returnToApplication: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _returnToAppError: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _isInitializing: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val _isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _editMode: MutableStateFlow<EditMode> = MutableStateFlow(EditMode.Trimming)
    private val _videoRotation: MutableStateFlow<Float> = MutableStateFlow(0f)
    private val _videoFrames: MutableStateFlow<ImmutableList<TimeLineFrame>> = MutableStateFlow(persistentListOf())

    // Noise clearing
    private val _isNoiseCleared: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _clearNoisedVideoPath: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _isProcessingClearNoise: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _useNoiseCanceledVideo: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val start: StateFlow<Long> =
        _start.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = _start.value
        )
    val end: StateFlow<Long> = _end.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _end.value
    )

    val isInitializing = _isInitializing.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _isInitializing.value
    )

    val isProcessing = _isProcessing.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _isProcessing.value
    )

    val resultPath = _returnToApplication.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _returnToApplication.value
    )

    val errorResult = _returnToAppError.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _returnToAppError.value
    )

    val videoInfo = _videoInfo.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _videoInfo.value
    )

    private fun calculateVideoAspectRatio(info: TruvideoSdkVideoInformation?): Float {
        if (info == null) {
            return 1f
        }


        val videos = info.videoTracks
        if (videos.isEmpty()) {
            return 1f
        }

        val video = videos.first()
        return video.rotatedWidth.toFloat() / video.rotatedHeight
    }

    val videoAspectRatio = _videoInfo.map { calculateVideoAspectRatio(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = calculateVideoAspectRatio(_videoInfo.value)
    )

    val volume = _volume.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _volume.value
    )

    val editMode = _editMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _editMode.value
    )

    val videoRotation = _videoRotation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _videoRotation.value
    )

    val videoFrames = _videoFrames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _videoFrames.value
    )

    // Noise cancelling
    val useNoiseCanceledVideo = _useNoiseCanceledVideo.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _useNoiseCanceledVideo.value
    )

    val isProcessingClearNoise = _isProcessingClearNoise.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _useNoiseCanceledVideo.value
    )

    val clearNoisedVideoPath = _clearNoisedVideoPath.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _clearNoisedVideoPath.value
    )

    val videoPath = combine(_useNoiseCanceledVideo, _clearNoisedVideoPath) { useNoiseCanceledVideo: Boolean, clearNoiseVideoPath ->
        if (useNoiseCanceledVideo) {
            clearNoiseVideoPath ?: ""
        } else {
            input.getPath(context)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = input.getPath(context)
    )


    private fun calculateWithChanges(
        videoDuration: Long,
        start: Long,
        end: Long,
        volume: Float,
        videoRotation: Float
    ): Boolean {
        if (start > 0L) return true
        if (end < videoDuration) return true
        if (volume != 1f) return true
        var rot = videoRotation % 360
        if (rot < 0) rot += 360
        if (rot != 0f) return true
        return false
    }

    val withChanges = combine(_videoInfo, _start, _end, _volume, _videoRotation) { info, start, end, volume, rotation ->
        calculateWithChanges(
            videoDuration = info?.durationMillis ?: 0L,
            start = start,
            end = end,
            volume = volume,
            videoRotation = rotation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = calculateWithChanges(
            videoDuration = _videoInfo.value?.durationMillis ?: 0L,
            start = _start.value,
            end = _end.value,
            volume = _volume.value,
            videoRotation = _videoRotation.value
        )
    )

    fun updateStart(value: Long) {
        _start.value = value
    }

    fun updateEnd(value: Long) {
        _end.value = value
    }

    fun updateVolume(value: Float) {
        _volume.value = value
    }

    fun updateEditionMode(value: EditMode) {
        _editMode.value = value
    }

    fun updateRotation(value: Float) {
        _videoRotation.value += value
    }


    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            if (previewMode) {
                _isInitializing.value = true
                _start.value = 0L
                _end.value = 1000L
                _videoInfo.value = TruvideoSdkVideoInformation.empty()
                _isInitializing.value = false
            } else {
                _isInitializing.value = true
                val info = getVideoInfoUseCase(input)
                _start.value = 0L
                _end.value = info.durationMillis
                _videoInfo.value = info
                _isInitializing.value = false
                fetchPreviews()
            }
        }
    }


    private suspend fun fetchPreviews() {
        _videoFrames.value = persistentListOf()
        val videoInfo = getVideoInfoUseCase(input)
        val videoLengthInMs = videoInfo.durationMillis

        val interval = videoLengthInMs / previewItemCount

        val start = System.currentTimeMillis()
        val filesToDelete = mutableListOf<String>()
        for (i in 0 until previewItemCount) {
            try {
                val thumbnailPath = createVideoThumbnailUseCase(
                    input = input,
                    output = thumbnailFileDescription,
                    position = (i * interval).toDuration(DurationUnit.MILLISECONDS),
                    width = 100,
                    precise = false
                )
                filesToDelete.add(thumbnailPath)

                val bitmap = BitmapFactory.decodeFile(thumbnailPath)
                val isActive = viewModelScope.isActive
                if (isActive) {
                    val currentList = videoFrames.value
                    _videoFrames.value = (currentList + TimeLineFrame(i, bitmap)).toPersistentList()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        filesToDelete.forEach {
            try {
                File(it).delete()
            } catch (_: Exception) {

            }
        }

        val end = System.currentTimeMillis()
        Log.d("TruvideoSdkVideo", "fetchPreviews takes ${end - start}ms")
    }

    fun apply() {
        _isProcessing.value = true
        _returnToAppError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val start = _start.value
                val end = _end.value
                val rotation = videoRotation.value
                val volume = volume.value

                val originalRotation = (videoInfo.value?.videoTracks?.firstOrNull()?.rotation ?: TruvideoSdkVideoRotation.DEGREES_0)
                var finalRotation = rotation + originalRotation.degrees
                finalRotation %= 360
                if (finalRotation < 0) finalRotation += 360

                val inputFilePath = input.getPath(context)
                val path = if (_isNoiseCleared.value && _useNoiseCanceledVideo.value) _clearNoisedVideoPath.value else inputFilePath

                val outputFilePath = editVideoUseCase(
                    input = TruvideoSdkVideoFile.custom(path ?: ""),
                    output = output,
                    startPosition = start,
                    endPosition = end,
                    volume = volume,
                    rotation = when (finalRotation) {
                        0f -> TruvideoSdkVideoRotation.DEGREES_0
                        90F -> TruvideoSdkVideoRotation.DEGREES_90
                        180F -> TruvideoSdkVideoRotation.DEGREES_180
                        270F -> TruvideoSdkVideoRotation.DEGREES_270
                        else -> null
                    },
                    printLogs = true
                )

                _returnToApplication.value = outputFilePath
            } catch (exception: Exception) {
                exception.printStackTrace()
                _isProcessing.value = false
                _returnToAppError.value = exception.localizedMessage
            }
        }
    }

    fun toggleUseNoiseCanceledVideo() {
        viewModelScope.launch(Dispatchers.IO) {
            processToggleNoiseCancelling()
        }
    }

    private suspend fun processToggleNoiseCancelling() {
        if (_isProcessingClearNoise.value) {
            Log.d("TruvideoSdkVideo", "Cant clear noise, its processing")
            return
        }

        val newValue = !_useNoiseCanceledVideo.value
        if (newValue) {
            Log.d("TruvideoSdkVideo", "Turning ON NC")
            if (!_isNoiseCleared.value) {
                Log.d("TruvideoSdkVideo", "the video its not noise cleared")

                _isProcessingClearNoise.value = true

                try {

                    clearVideoAudioNoiseUseCase(
                        input = input,
                        output = ncFileDescription
                    )

                    val outputFilePath = ncFileDescription.getPath(context, File(input.getPath(context)).extension)
                    _clearNoisedVideoPath.value = outputFilePath
                    _isNoiseCleared.value = true
                    _useNoiseCanceledVideo.value = true
                    Log.d("TruvideoSdkVideo", "Clearing noise process finished")
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    Log.d("TruvideoSdkVideo", "Clearing noise process error ${exception.localizedMessage}")
                }
                _isProcessingClearNoise.value = false
            } else {
                Log.d("TruvideoSdkVideo", "NC its ready. turning on")
                _useNoiseCanceledVideo.value = true
            }
        } else {
            Log.d("TruvideoSdkVideo", "Turning OFF NC")
            _useNoiseCanceledVideo.value = false
        }
    }


    fun close() {
        try {
            val ncFilePath = ncFileDescription.getPath(context, File(input.getPath(context)).extension)
            val file = File(ncFilePath)
            if (file.exists()) file.delete()
        } catch (_: Exception) {

        }

        viewModelScope.cancel()
    }
}