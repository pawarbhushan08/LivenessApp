package com.bhushan.android.presentation.camera.utils

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

object YuvToRgbConverter {

    /**
     * Converts [ImageProxy] in YUV_420_888 format to RGB [Bitmap].
     * This implementation does not use deprecated RenderScript.
     */
    fun yuvToRgb(image: ImageProxy, output: Bitmap) {
        val yPlane = image.planes[0].buffer
        val uPlane = image.planes[1].buffer
        val vPlane = image.planes[2].buffer

        val yRowStride = image.planes[0].rowStride
        val uvRowStride = image.planes[1].rowStride
        val uvPixelStride = image.planes[1].pixelStride

        val width = image.width
        val height = image.height

        val argb8888 = IntArray(width * height)

        yPlane.rewind()
        uPlane.rewind()
        vPlane.rewind()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val yIndex = y * yRowStride + x
                val uvRow = y shr 1
                val uvCol = x shr 1

                val uIndex = uvRow * uvRowStride + uvCol * uvPixelStride
                val vIndex = uvRow * uvRowStride + uvCol * uvPixelStride

                val yValue = (yPlane.get(yIndex).toInt() and 0xFF)
                val uValue = (uPlane.get(uIndex).toInt() and 0xFF)
                val vValue = (vPlane.get(vIndex).toInt() and 0xFF)

                // Convert YUV to RGB (BT.601)
                val yNorm = yValue - 16
                val uNorm = uValue - 128
                val vNorm = vValue - 128

                val r = (1.164f * yNorm + 1.596f * vNorm).toInt().coerceIn(0, 255)
                val g = (1.164f * yNorm - 0.392f * uNorm - 0.813f * vNorm).toInt().coerceIn(0, 255)
                val b = (1.164f * yNorm + 2.017f * uNorm).toInt().coerceIn(0, 255)

                argb8888[y * width + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        output.setPixels(argb8888, 0, width, 0, 0, width, height)
    }
}