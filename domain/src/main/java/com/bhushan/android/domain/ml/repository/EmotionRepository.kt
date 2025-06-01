package com.bhushan.android.domain.ml.repository

import com.bhushan.android.domain.ml.model.ImageData

interface EmotionRepository {
    suspend fun detectEmotionTFLite(image: ImageData): String
    suspend fun detectEmotionOnnx(image: ImageData): String
}