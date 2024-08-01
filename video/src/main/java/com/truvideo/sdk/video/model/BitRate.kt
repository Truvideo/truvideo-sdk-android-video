package com.truvideo.sdk.video.model

enum class BitRate(private val unit: Int, private val dimension: BitDimension) {
    Regular(320, BitDimension.Kilobytes);

    override fun toString(): String {
        return "$unit$dimension"
    }
}

enum class BitDimension(private val description: String) {
    Kilobytes("k"),
    Megabytes("M");

    override fun toString(): String {
        return description
    }
}