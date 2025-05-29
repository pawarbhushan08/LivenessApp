package com.bhushan.android.presentation.camera.model

sealed class CameraIntent {
    object BindCamera : CameraIntent()
    data class PermissionResult(val granted: Boolean) : CameraIntent()
    object UnbindCamera : CameraIntent()
}