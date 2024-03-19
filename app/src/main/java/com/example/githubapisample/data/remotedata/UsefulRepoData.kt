package com.example.githubapisample.data.remotedata

data class RepoResult (
    val totalCount: Int,
    val repoDataList: List<RepoData>
)

data class RepoData (
    val id: Long,
    val fullName: String,
    val description: String,
    val updatedAt: String,
    val stargazersCount: Int,
    val language: String,
    val avatarUrl: String
)

sealed class GitHubResponse {
    data class Success(val repoResult: RepoResult) : GitHubResponse()
    data class Error(val message: String) : GitHubResponse()
}