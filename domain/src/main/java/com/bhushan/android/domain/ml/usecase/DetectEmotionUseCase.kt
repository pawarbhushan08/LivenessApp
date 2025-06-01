package com.bhushan.android.domain.ml.usecase

import com.bhushan.android.domain.ml.model.EmotionResult
import com.bhushan.android.domain.ml.model.ImageData
import com.bhushan.android.domain.ml.repository.EmotionRepository

class DetectEmotionUseCase(
    private val repo: EmotionRepository
) {
    suspend operator fun invoke(
        tfLiteImageData: ImageData,
        onnxImageData: ImageData
    ): EmotionResult {
        val tflite = repo.detectEmotionTFLite(tfLiteImageData)
        val onnx = repo.detectEmotionOnnx(onnxImageData)
        return EmotionResult(tflite, onnx)
    }
}