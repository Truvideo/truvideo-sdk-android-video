package com.truvideo.sdk.video.ui.utils

import android.content.Context
import android.util.TypedValue

object ContextUtils {
    fun Context.dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

    fun Context.spToPx(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }

    fun Context.pxToDp(px: Float): Float {
        val density = resources.displayMetrics.density
        return px / density
    }
}