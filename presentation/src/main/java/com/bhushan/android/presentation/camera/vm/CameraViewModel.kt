package com.bhushan.android.presentation.camera.vm

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.bhushan.android.presentation.camera.model.CameraIntent
import com.bhushan.android.presentation.camera.model.CameraViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraViewState())
    val state: StateFlow<CameraViewState> = _state.asStateFlow()

    private var cameraJob: Job? = null
    private var processCameraProvider: ProcessCameraProvider? = null

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _state.update { it.copy(surfaceRequest = newSurfaceRequest) }
        }
    }

    fun handleIntent(intent: CameraIntent) {
        when (intent) {
            is CameraIntent.PermissionResult -> {
                _state.update { it.copy(hasPermission = intent.granted) }
            }

            is CameraIntent.BindCamera -> {
                if (_state.value.hasPermission && !_state.value.isCameraBound) {
                    bindCameraInternal(intent.context, intent.lifecycleOwner)
                }
            }
            is CameraIntent.UnbindCamera -> {
                unbindCameraInternal()
            }
        }
    }

    private fun bindCameraInternal(context: Context?, lifecycleOwner: LifecycleOwner?) {
        cameraJob?.cancel()
        cameraJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                processCameraProvider =
                    ProcessCameraProvider.awaitInstance(context = context ?: return@launch)
                val hasCamera = CameraSelector.DEFAULT_BACK_CAMERA
                    .filter(processCameraProvider!!.availableCameraInfos).isNotEmpty()
                if (hasCamera) {
                    processCameraProvider!!.bindToLifecycle(
                        lifecycleOwner ?: return@launch,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        cameraPreviewUseCase
                    )
                    _state.update { it.copy(isCameraBound = true, error = null) }
                } else {
                    _state.update { it.copy(error = "No back camera found") }
                }
                awaitCancellation()
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Camera binding error", e)
                _state.update { it.copy(error = e.message) }
            } finally {
                processCameraProvider?.unbindAll()
                _state.update { it.copy(isCameraBound = false, surfaceRequest = null) }
            }
        }
    }

    private fun unbindCameraInternal() {
        cameraJob?.cancel()
        processCameraProvider?.unbindAll()
        _state.update { it.copy(isCameraBound = false, surfaceRequest = null) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraJob?.cancel()
        processCameraProvider?.unbindAll()
    }
}