package com.bhushan.android.domain.ml.model

data class ImageData(
    val pixels: FloatArray,
    val width: Int,
    val height: Int,
    val format: Format
) {
    enum class Format { GRAYSCALE, RGB }
}