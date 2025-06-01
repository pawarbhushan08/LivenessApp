package di

import com.bhushan.android.presentation.camera.vm.CameraViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { CameraViewModel(get()) }
}