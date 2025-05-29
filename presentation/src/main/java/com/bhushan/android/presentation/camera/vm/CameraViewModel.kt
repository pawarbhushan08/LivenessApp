package com.bhushan.android.presentation.camera.vm

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraViewState())
    val state: StateFlow<CameraViewState> = _state.asStateFlow()

    private val intentChannel = MutableSharedFlow<CameraIntent>(extraBufferCapacity = 8)
    private var cameraJob: Job? = null
    private var processCameraProvider: ProcessCameraProvider? = null

    // Provide context/lifecycle if needed (e.g., via setters or processIntent)
    private var currentContext: Context? = null
    private var currentLifecycleOwner: LifecycleOwner? = null

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _state.update { it.copy(surfaceRequest = newSurfaceRequest) }
        }
    }

    init {
        CoroutineScope(Dispatchers.Main).launch {
            intentChannel.collect { intent ->
                handleIntent(intent)
            }
        }
    }

    fun processIntent(intent: CameraIntent) {
        intentChannel.tryEmit(intent)
    }

    private fun handleIntent(intent: CameraIntent) {
        when (intent) {
            is CameraIntent.PermissionResult -> {
                _state.update { it.copy(hasPermission = intent.granted) }
            }
            is CameraIntent.BindCamera -> {
                if (_state.value.hasPermission && !_state.value.isCameraBound) {
                    bindCameraInternal()
                }
            }
            is CameraIntent.UnbindCamera -> {
                unbindCameraInternal()
            }
        }
    }

    private fun bindCameraInternal() {
        cameraJob?.cancel()
        cameraJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val ctx = currentContext as? Context ?: return@launch
                val owner = currentLifecycleOwner as? LifecycleOwner ?: return@launch
                processCameraProvider = ProcessCameraProvider.awaitInstance(context = ctx)
                val hasCamera = CameraSelector.DEFAULT_BACK_CAMERA
                    .filter(processCameraProvider!!.availableCameraInfos).isNotEmpty()
                if (hasCamera) {
                    processCameraProvider!!.bindToLifecycle(
                        owner, CameraSelector.DEFAULT_BACK_CAMERA, cameraPreviewUseCase
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

    fun setContextAndOwner(context: Context, lifecycleOwner: LifecycleOwner) {
        this.currentContext = context
        this.currentLifecycleOwner = lifecycleOwner
    }

    override fun onCleared() {
        super.onCleared()
        cameraJob?.cancel()
        processCameraProvider?.unbindAll()
    }
}