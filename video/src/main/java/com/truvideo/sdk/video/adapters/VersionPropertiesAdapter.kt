package com.truvideo.sdk.video.adapters

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class VersionPropertiesAdapter(
    val context: Context
) {

    fun readProperty(propertyName: String): String? {
        val assetManager = context.assets

        val filename = "version.properties"
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