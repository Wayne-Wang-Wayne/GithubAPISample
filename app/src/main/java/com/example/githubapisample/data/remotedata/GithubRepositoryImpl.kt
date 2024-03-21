package com.example.githubapisample.data.remotedata

import com.example.githubapisample.data.GithubRepository
import com.example.githubapisample.utils.GitHubApiDataMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher

class GithubRepositoryImpl(
    private val gitHubApiService: GitHubApiService,
    private val gitHubApiDataMapper: GitHubApiDataMapper,
    private val ioDispatcher: CoroutineDispatcher
) : GithubRepository {

    override suspend fun searchRepositories(
        query: String,
        perPage: Int,
        page: Int
    ): GitHubResponse =
        try {
            val response = gitHubApiService.searchRepositories(query, perPage, page)
            if (response.isSuccessful) {
                if (response.body() == null) {
                    GitHubResponse.Error("Failed to fetch data: response body is null")
                } else {
                    gitHubApiDataMapper.toSuccessGitHubResponse(response.body()!!)
                }
            } else {
                GitHubResponse.Error("Failed to fetch data: ${response.code()}")
            }
        } catch (e: CancellationException) {
            GitHubResponse.Cancel
        } catch (e: Exception) {
            GitHubResponse.Error("Error: ${e.message}")
        }
}