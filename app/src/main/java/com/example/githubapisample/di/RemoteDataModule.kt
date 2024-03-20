package com.example.githubapisample.di

import com.example.githubapisample.data.remotedata.GitHubApiService
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api.github.com/"

private const val CONNECT_TIMEOUT = 10L

val remoteDataModule = module {
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    factory<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<GitHubApiService> { get<Retrofit>().create(GitHubApiService::class.java) }
}