package di

import com.bhushan.android.presentation.camera.utils.ONNXModelHelper
import com.bhushan.android.presentation.camera.utils.ONNXModelLoader
import com.bhushan.android.presentation.camera.utils.TFLiteModelLoader
import com.bhushan.android.presentation.camera.vm.CameraViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.tensorflow.lite.Interpreter

val presentationModule = module {
    single { ONNXModelHelper(ONNXModelLoader.loadModelFile(androidContext(), "model.onnx")) }
    single<Interpreter> { TFLiteModelLoader.loadModel(get(), "emotion_model.tflite") }
    viewModel { CameraViewModel(get(), get()) }
}