package com.example.githubapisample.di

import com.example.githubapisample.data.GithubRepository
import com.example.githubapisample.data.remotedata.GithubRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<GithubRepository> { GithubRepositoryImpl(get(), get()) }
}