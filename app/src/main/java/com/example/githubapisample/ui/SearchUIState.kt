package com.example.githubapisample.ui

import com.example.githubapisample.data.remotedata.RepoData

data class SearchUIState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val searchQuery: String = "",
    val repositories: List<RepoData> = emptyList()
)
