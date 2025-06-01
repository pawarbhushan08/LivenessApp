package com.bhushan.android.data.utils

import com.bhushan.android.domain.ml.model.ImageData


object PreprocessingUtils {
    fun toTFLiteInput(image: ImageData): Array<Array<Array<FloatArray>>> {
        // Assuming input is already grayscale and 48x48
        // Convert ImageData.pixels to the right shape for TFLite
        val size = 48
        val input = Array(1) { Array(size) { Array(size) { FloatArray(1) } } }
        for (y in 0 until size) {
            for (x in 0 until size) {
                val idx = y * size + x
                input[0][y][x][0] = image.pixels[idx]
            }
        }
        return input
    }

    fun toOnnxInput(image: ImageData): Array<Array<Array<FloatArray>>> {
        // Assuming input is already RGB and 224x224
        val width = 224
        val height = 224
        val input = Array(1) { Array(3) { Array(height) { FloatArray(width) } } }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val baseIdx = (y * width + x) * 3
                input[0][0][y][x] = image.pixels[baseIdx]     // R
                input[0][1][y][x] = image.pixels[baseIdx + 1] // G
                input[0][2][y][x] = image.pixels[baseIdx + 2] // B
            }
        }
        return input
    }
}