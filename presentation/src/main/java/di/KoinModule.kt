package di

import com.bhushan.android.presentation.camera.utils.TFLiteModelLoader
import com.bhushan.android.presentation.camera.vm.CameraViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.tensorflow.lite.Interpreter

val presentationModule = module {
    single<Interpreter> { TFLiteModelLoader.loadModel(get(), "emotion_model.tflite") }
    viewModel { CameraViewModel(get()) }
}