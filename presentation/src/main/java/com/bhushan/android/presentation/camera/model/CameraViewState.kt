package com.bhushan.android.presentation.camera.model

import androidx.camera.core.SurfaceRequest
import com.bhushan.android.presentation.camera.vm.ModelType

data class CameraViewState(
    val hasPermission: Boolean = false,
    val isCameraBound: Boolean = false,
    val surfaceRequest: SurfaceRequest? = null,
    val error: String? = null,
    val isLoadings: Boolean = false,
    val emotionResult: String? = null,
    val modelType: ModelType = ModelType.TFLITE
)