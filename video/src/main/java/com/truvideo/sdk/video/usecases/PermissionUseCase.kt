package com.truvideo.sdk.video.usecases

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

internal class PermissionUseCase(val context: Context) {

    private var handlers = mutableMapOf<ComponentActivity, TruvideoSdkPermissionHandler>()

    fun init(activity: ComponentActivity): TruvideoSdkPermissionHandler {
        var handler = handlers[activity]
        if (handler == null) {
            handler = TruvideoSdkPermissionHandler(
                activity = activity,
                readStoragePermissions = readStoragePermissions,
                writeStoragePermissions = writeStoragePermissions
            )
            handlers[activity] = handler
        }

        return handler
    }

    private val readStoragePermissions: List<String>
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            } else {
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }
        }

    private val writeStoragePermissions: List<String>
        get() {
            return listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }


    fun hasWriteStoragePermission() = has(writeStoragePermissions)

    fun hasReadStoragePermission() = has(readStoragePermissions)

    private fun has(permissions: List<String>): Boolean {
        permissions.forEach {
            val permission = ContextCompat.checkSelfPermission(context, it)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }
}

class TruvideoSdkPermissionHandler(
    private val activity: ComponentActivity,
    private val writeStoragePermissions: List<String>,
    private val readStoragePermissions: List<String>
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var continuation: CancellableContinuation<Boolean>? = null
    private val startForResult: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val allGranted = it.values.all { permission -> permission }
            continuation?.resumeWith(Result.success(allGranted))
        }


    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun askWriteStoragePermission() = ask(writeStoragePermissions)

    @Suppress("unused")
    fun askWriteStoragePermission(callback: TruvideoSdkVideoCallback<Boolean>) {
        scope.launch {
            val result = askWriteStoragePermission()
            callback.onComplete(result)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun askReadStoragePermission() = ask(readStoragePermissions)

    @Suppress("unused")
    fun askReadStoragePermission(callback: TruvideoSdkVideoCallback<Boolean>) {
        scope.launch {
            val result = askReadStoragePermission()
            callback.onComplete(result)
        }
    }

    @Suppress("unused")
    fun hasWriteStoragePermission() = has(writeStoragePermissions)

    @Suppress("unused")
    fun hasReadStoragePermission() = has(readStoragePermissions)

    private suspend fun ask(permission: List<String>): Boolean {
        startForResult.launch(permission.toTypedArray())
        return suspendCancellableCoroutine { continuation = it }
    }

    private fun has(permissions: List<String>): Boolean {
        permissions.forEach {
            val permission = ContextCompat.checkSelfPermission(activity, it)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }
}