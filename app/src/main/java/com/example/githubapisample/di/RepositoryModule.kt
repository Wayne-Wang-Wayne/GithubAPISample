package com.example.githubapisample.di

import com.example.githubapisample.data.GithubRepository
import com.example.githubapisample.data.remotedata.GithubRepositoryImpl
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val IO_DISPATCHER = "IODispatcher"

val repositoryModule = module {
    single<GithubRepository> { GithubRepositoryImpl(get(), get(), get(named(IO_DISPATCHER))) }

    single(named(IO_DISPATCHER)) {
        Dispatchers.IO
    }

}