package com.bhushan.android.domain.ml.model

data class EmotionResult(
    val tfliteEmotion: String,
    val onnxEmotion: String
)