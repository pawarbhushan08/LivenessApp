package com.bhushan.android.presentation.camera.model

import android.content.Context
import androidx.lifecycle.LifecycleOwner

sealed class CameraIntent {
    data class BindCamera(val context: Context, val lifecycleOwner: LifecycleOwner) : CameraIntent()
    data class PermissionResult(val granted: Boolean) : CameraIntent()
    object UnbindCamera : CameraIntent()
}