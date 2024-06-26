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
                                    totalCount = 1000,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 1,
                                            fullName = "google/android",
                                            description = "android1",
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
                                    totalCount = 1000,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 2,
                                            fullName = "google/android",
                                            description = "android2",
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
                        3 -> {
                            GitHubResponse.Success(
                                RepoResult(
                                    totalCount = 1000,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 3,
                                            fullName = "google/android",
                                            description = "android3",
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
                                    totalCount = 10,
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
                                    totalCount = 1000,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 3,
                                            fullName = "apple/ios",
                                            description = "ios1",
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
                                    totalCount = 1000,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 4,
                                            fullName = "apple/ios",
                                            description = "ios2",
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
                        3 -> {
                            GitHubResponse.Success(
                                RepoResult(
                                    totalCount = 1000,
                                    repoDataList = listOf(
                                        RepoData(
                                            id = 5,
                                            fullName = "apple/ios",
                                            description = "ios3",
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
                                    totalCount = 10,
                                    repoDataList = emptyList()
                                )
                            )
                        }
                    }
                }
                "tooMuch" -> {
                    val list = List(30) { currPage ->
                        RepoResult(
                            totalCount = 100000000,
                            repoDataList = List(3000) {
                                RepoData(
                                    id = it.toLong(),
                                    fullName = "google/android",
                                    description = "tooMuch${currPage + 1}",
                                    updatedAt = "2021-01-01T00:00:00Z",
                                    stargazersCountS = "1",
                                    language = "Java",
                                    avatarUrl = "https://avatars.githubusercontent.com/u/1342004?v=4",
                                    repoUrl = ""
                                )
                            }
                        )
                    }
                    GitHubResponse.Success(
                        list[page - 1]
                    )
                }

                else -> {
                    GitHubResponse.Success(
                        RepoResult(
                            totalCount = 1000,
                            repoDataList = emptyList()
                        )
                    )
                }
            }

        }
    }
}