package com.bhushan.android.presentation.camera.ui

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bhushan.android.presentation.camera.model.CameraIntent
import com.bhushan.android.presentation.camera.model.CameraViewState
import com.bhushan.android.presentation.camera.vm.CameraViewModel
import com.bhushan.android.presentation.camera.vm.ModelType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraViewScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // When permission changes, dispatch intent
    LaunchedEffect(cameraPermissionState.status) {
        viewModel.handleIntent(CameraIntent.PermissionResult(cameraPermissionState.status.isGranted))
        if (cameraPermissionState.status.isGranted) {
            viewModel.handleIntent(CameraIntent.BindCamera(context, lifecycleOwner))
        } else {
            viewModel.handleIntent(CameraIntent.UnbindCamera)
        }
    }

    // RELEASE CAMERA when composable leaves composition
    androidx.compose.runtime.DisposableEffect(cameraPermissionState.status.isGranted) {
        onDispose {
            viewModel.handleIntent(CameraIntent.UnbindCamera)
        }
    }

    if (state.hasPermission) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(0.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Camera view occupies 70%
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
            ) {
                CameraViewContent(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                )
            }

            ModelSelector(
                modifier = Modifier.weight(0.1f),
                selectedModel = state.modelType,
                onModelSelected = {
                    viewModel.selectModel(it)
                    // After selection, if permission and bound, rebind
                    viewModel.handleIntent(CameraIntent.BindCamera(context, lifecycleOwner))
                }
            )

            // Result text occupies 30%
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
                    .padding(24.dp),
            ) {
                val text = when (state.modelType) {
                    ModelType.TFLITE -> "Emotion TFLite: ${state.emotionResult}"
                    else -> "Emotion ONNX: ${state.emotionResult}"
                }
                Text(
                    text = text,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    } else {
        // ... your permission rationale UI ...
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                "Hey there, superstar! We need a peek through your camera to make the magic happen.✨"
            } else {
                "Hi there! We need your camera to work our magic! ✨\nGrant us permission and let's get this party started! 🎉"
            }
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Unleash the Camera!")
            }
        }
    }

    // Optionally show errors from state
    state.error?.let { error ->
        // Show error dialog/toast/snackbar as needed
    }
}

@Composable
fun ModelSelector(
    modifier: Modifier = Modifier,
    selectedModel: ModelType,
    onModelSelected: (ModelType) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButtonWithLabel(
            label = "TFLite",
            selected = selectedModel == ModelType.TFLITE,
            onClick = { onModelSelected(ModelType.TFLITE) }
        )
        Spacer(Modifier.width(24.dp))
        RadioButtonWithLabel(
            label = "ONNX",
            selected = selectedModel == ModelType.ONNX,
            onClick = { onModelSelected(ModelType.ONNX) }
        )
    }
}

@Composable
fun RadioButtonWithLabel(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text = label)
    }
}

@Composable
fun CameraViewContent(
    state: CameraViewState,
    modifier: Modifier = Modifier,
) {
    state.surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier
        )
    }
}