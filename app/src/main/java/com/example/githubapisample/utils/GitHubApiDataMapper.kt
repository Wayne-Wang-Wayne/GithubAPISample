package com.example.githubapisample.utils

import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.data.remotedata.RepoData
import com.example.githubapisample.data.remotedata.RepoResult
import com.example.githubapisample.data.remotedata.SearchResult

class GitHubApiDataMapper {

    fun toGitHubResponse(searchResult: SearchResult): GitHubResponse = searchResult.run {
        GitHubResponse.Success(
            RepoResult(
                totalCount = totalCount ?: 0,
                repoDataList = items?.map {
                    RepoData(
                        id = it.id ?: 0L,
                        fullName = it.fullName ?: "",
                        description = it.description ?: "",
                        updatedAt = it.updatedAt ?: "",
                        stargazersCount = it.stargazersCount ?: 0,
                        language = it.language ?: "",
                        avatarUrl = it.owner?.avatarUrl ?: ""
                    )
                } ?: emptyList()
            )
        )
    }

}