package com.example.githubapisample.ui

import android.app.Application
import com.example.githubapisample.di.remoteDataModule
import com.example.githubapisample.di.repositoryModule
import com.example.githubapisample.di.utilsModule
import com.example.githubapisample.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val appModules = listOf(viewModelModule, repositoryModule, remoteDataModule, utilsModule)
        startKoin {
            androidContext(this@MyApplication)
            modules(appModules)
        }
    }
}