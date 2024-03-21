package com.example.githubapisample.ui

import com.example.githubapisample.data.remotedata.GitHubResponse

data class SearchMoreParam(
    val nextPage: Int,
    val onSuccess: (Int, GitHubResponse.Success) -> Unit,
    val onError: (GitHubResponse.Error) -> Unit
)