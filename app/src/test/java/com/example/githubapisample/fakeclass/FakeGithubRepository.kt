package com.example.githubapisample.fakeclass

import com.example.githubapisample.data.GithubRepository
import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.data.remotedata.RepoData
import com.example.githubapisample.data.remotedata.RepoResult
import kotlinx.coroutines.delay

const val FAKE_REPOSITORY_SEARCH_TIME = 1000L

class FakeGithubRepository : GithubRepository {

    var shouldReturnError = false
    override suspend fun searchRepositories(
        query: String,
        perPage: Int,
        page: Int
    ): GitHubResponse {
        delay(FAKE_REPOSITORY_SEARCH_TIME)
        return if (shouldReturnError) {
            GitHubResponse.Error("Error")
        } else {
            when (query) {
                "android" -> {
                    when (page) {
                        1 -> {
                            GitHubResponse.Success(
                                RepoResult(
                                    totalCount = 1,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 1,
                                            fullName = "google/android",
                                            description = "Android",
                                            updatedAt = "2021-01-01T00:00:00Z",
                                            stargazersCountS = "1",
                                            language = "Java",
                                            avatarUrl = "https://avatars.githubusercontent.com/u/1342004?v=4",
                                            repoUrl = ""
                                        )
                                    )
                                )
                            )
                        }

                        2 -> {
                            GitHubResponse.Success(
                                RepoResult(
                                    totalCount = 2,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 2,
                                            fullName = "google/android",
                                            description = "Android",
                                            updatedAt = "2021-01-01T00:00:00Z",
                                            stargazersCountS = "1",
                                            language = "Java",
                                            avatarUrl = "https://avatars.githubusercontent.com/u/1342004?v=4",
                                            repoUrl = ""
                                        )
                                    )
                                )
                            )
                        }
                        else -> {
                            GitHubResponse.Success(
                                RepoResult(
                                    totalCount = 0,
                                    repoDataList = emptyList()
                                )
                            )
                        }
                    }
                }

                "ios" -> {
                    when (page) {
                        1 -> {
                            GitHubResponse.Success(
                                RepoResult(
                                    totalCount = 1,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 3,
                                            fullName = "apple/ios",
                                            description = "iOS",
                                            updatedAt = "2021-01-01T00:00:00Z",
                                            stargazersCountS = "1",
                                            language = "Swift",
                                            avatarUrl = "https://avatars.githubusercontent.com/u/10639145?v=4",
                                            repoUrl = ""
                                        )
                                    )
                                )
                            )
                        }

                        2 -> {
                            GitHubResponse.Success(
                                RepoResult(
                                    totalCount = 2,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 4,
                                            fullName = "apple/ios",
                                            description = "iOS",
                                            updatedAt = "2021-01-01T00:00:00Z",
                                            stargazersCountS = "1",
                                            language = "Swift",
                                            avatarUrl = "https://avatars.githubusercontent.com/u/10639145?v=4",
                                            repoUrl = ""
                                        )
                                    )
                                )
                            )
                        }

                        else -> {
                            GitHubResponse.Success(
                                RepoResult(
                                    totalCount = 0,
                                    repoDataList = emptyList()
                                )
                            )
                        }
                    }
                }

                else -> {
                    GitHubResponse.Success(
                        RepoResult(
                            totalCount = 0,
                            repoDataList = emptyList()
                        )
                    )
                }
            }

        }
    }
}