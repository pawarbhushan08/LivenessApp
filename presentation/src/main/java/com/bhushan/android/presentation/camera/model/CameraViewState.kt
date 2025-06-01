package com.bhushan.android.presentation.camera.model

import androidx.camera.core.SurfaceRequest

data class CameraViewState(
    val hasPermission: Boolean = false,
    val isCameraBound: Boolean = false,
    val surfaceRequest: SurfaceRequest? = null,
    val error: String? = null,
    val isLoadings: Boolean = false,
    val emotionTFLite: String? = null,
    val emotionONNX: String? = null
)