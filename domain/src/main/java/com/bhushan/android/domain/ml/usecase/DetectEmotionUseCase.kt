package com.bhushan.android.domain.ml.usecase

import com.bhushan.android.domain.ml.model.EmotionResult
import com.bhushan.android.domain.ml.model.ImageData
import com.bhushan.android.domain.ml.repository.EmotionRepository

class DetectEmotionUseCase(
    private val repo: EmotionRepository
) {
    suspend operator fun invoke(
        tfLiteImageData: ImageData,
        selectedModel: MLModelType
    ): EmotionResult {
        return when (selectedModel) {
            MLModelType.TFLITE -> {
                EmotionResult(repo.detectEmotionTFLite(tfLiteImageData))
            }

            MLModelType.ONNX -> {
                EmotionResult(repo.detectEmotionOnnx(tfLiteImageData))
            }
        }
    }
}

enum class MLModelType {
    TFLITE, ONNX
}