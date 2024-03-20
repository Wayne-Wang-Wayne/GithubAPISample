package com.example.githubapisample.di

import com.example.githubapisample.ui.SearchViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single<SearchViewModel> {
        SearchViewModel(get())
    }
}