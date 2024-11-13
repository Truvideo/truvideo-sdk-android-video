package com.truvideo.sdk.video.adapters

import android.content.Context
import android.content.res.AssetManager
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoVersionPropertiesAdapter
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

internal class TruvideoSdkVideoVersionPropertiesAdapterImpl(
    context: Context
) : TruvideoSdkVideoVersionPropertiesAdapter {

    private val assetManager: AssetManager = context.assets

    override fun readProperty(propertyName: String): String? {
        val filename = "version-video.properties"

        return try {
            val inputStream = assetManager.open(filename)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line?.split("=")
                if (parts?.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()

                    if (key == propertyName) {
                        reader.close()
                        return value
                    }
                }
            }

            reader.close()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}