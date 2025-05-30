package com.bhushan.android.presentation.camera.utils

import android.graphics.Bitmap

object PreprocessingUtils {
    fun preprocessGray(bitmap: Bitmap, inputSize: Int): Array<Array<Array<FloatArray>>> {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val input = Array(1) { Array(inputSize) { Array(inputSize) { FloatArray(1) } } }
        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = resized.getPixel(x, y)
                // Convert to grayscale
                val r = (pixel shr 16 and 0xFF)
                val g = (pixel shr 8 and 0xFF)
                val b = (pixel and 0xFF)
                val gray = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f
                input[0][y][x][0] = gray
            }
        }
        return input
    }
}