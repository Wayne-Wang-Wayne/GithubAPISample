package com.example.githubapisample.di

import com.example.githubapisample.utils.CountConverter
import com.example.githubapisample.utils.CountConverterImpl
import com.example.githubapisample.utils.GitHubApiDataMapper
import com.example.githubapisample.utils.GitHubApiDataMapperImpl
import com.example.githubapisample.utils.TimeConverter
import com.example.githubapisample.utils.TimeConverterImpl
import org.koin.dsl.module

val utilsModule = module {
    single<GitHubApiDataMapper> {
        GitHubApiDataMapperImpl(get(), get())
    }
    single<TimeConverter> {
        TimeConverterImpl()
    }
    single<CountConverter> {
        CountConverterImpl()
    }
}