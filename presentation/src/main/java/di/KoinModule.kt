package di

import com.bhushan.android.presentation.camera.utils.assets.AssetRepository
import com.bhushan.android.presentation.camera.utils.assets.AssetRepositoryImpl
import com.bhushan.android.presentation.camera.vm.CameraViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    single<AssetRepository> { AssetRepositoryImpl(get()) }
    viewModel {
        CameraViewModel(get())
    }
}