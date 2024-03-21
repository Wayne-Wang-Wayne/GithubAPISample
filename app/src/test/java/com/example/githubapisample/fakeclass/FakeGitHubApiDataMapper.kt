package com.example.githubapisample.fakeclass

import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.data.remotedata.RepoData
import com.example.githubapisample.data.remotedata.RepoResult
import com.example.githubapisample.data.remotedata.SearchResult
import com.example.githubapisample.utils.GitHubApiDataMapper

class FakeGitHubApiDataMapper : GitHubApiDataMapper {
    override fun toSuccessGitHubResponse(searchResult: SearchResult): GitHubResponse = searchResult.run {
        GitHubResponse.Success(
            RepoResult(
                totalCount = totalCount ?: 0,
                repoDataList = items?.map {
                    RepoData(
                        id = it.id ?: 0L,
                        fullName = "",
                        description = "",
                        updatedAt = "",
                        stargazersCountS = "",
                        language = "",
                        avatarUrl = "",
                        repoUrl = ""
                    )
                } ?: emptyList()
            )
        )
    }
}