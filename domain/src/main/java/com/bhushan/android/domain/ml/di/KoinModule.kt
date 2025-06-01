package com.bhushan.android.domain.ml.di

import com.bhushan.android.domain.ml.usecase.DetectEmotionUseCase
import org.koin.dsl.module

val domainModule = module {
    single { DetectEmotionUseCase(get()) }
}