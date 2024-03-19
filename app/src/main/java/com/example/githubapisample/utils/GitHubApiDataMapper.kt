package com.example.githubapisample.utils

import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.data.remotedata.SearchResult

interface GitHubApiDataMapper {
    fun toSuccessGitHubResponse(searchResult: SearchResult): GitHubResponse
}