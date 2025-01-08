package com.truvideo.noisecancel

object NoiseCancelNative {

    external fun globalInit(var0: String?)

    external fun setModel(var0: String?, var1: String?)

    external fun globalDestroy()

    external fun createSession(var0: String?)

    external fun closeSession()

    external fun processNCChunk(data: ByteArray?, chunkLength: Int) : ByteArray
}