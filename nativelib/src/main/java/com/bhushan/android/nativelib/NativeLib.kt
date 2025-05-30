package com.bhushan.android.nativelib

object NativeLib {

    /**
     * A native method that is implemented by the 'nativelib' native library,
     * which is packaged with this application.
     */
    external fun nativeDetectBlink(
        yBytes: ByteArray,
        width: Int,
        height: Int,
        faceCascadePath: String,
        eyeCascadePath: String
    ): Boolean

    // Used to load the 'nativelib' library on application startup.
    init {
        System.loadLibrary("nativelib")
    }

}