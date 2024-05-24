package com.truvideo.sdk.video.extensions

import org.json.JSONException
import org.json.JSONObject

internal fun JSONObject.getStringOrEmpty(key: String): String {
    return try {
        getString(key) ?: ""
    } catch (e: JSONException) {
        ""
    }
}

internal fun JSONObject.getIntOrZero(key: String): Int {
    return try {
        getInt(key)
    } catch (e: JSONException) {
        0
    }
}