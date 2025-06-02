package com.bhushan.android.livenessapp.di

import com.bhushan.android.data.di.dataModule
import com.bhushan.android.domain.ml.di.domainModule
import di.presentationModule

val appModules: List<org.koin.core.module.Module> =
    listOf(presentationModule, dataModule, domainModule)