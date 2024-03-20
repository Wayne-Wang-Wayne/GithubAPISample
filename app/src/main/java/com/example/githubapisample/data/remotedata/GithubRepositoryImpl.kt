package com.example.githubapisample.data.remotedata

import com.example.githubapisample.data.GithubRepository
import com.example.githubapisample.utils.GitHubApiDataMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GithubRepositoryImpl(
    private val gitHubApiService: GitHubApiService,
    private val gitHubApiDataMapper: GitHubApiDataMapper,
    private val ioDispatcher: CoroutineDispatcher
) : GithubRepository {

    override suspend fun searchRepositories(
        query: String,
        perPage: Int,
        page: Int
    ): GitHubResponse = withContext(ioDispatcher) {
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
        } catch (e: Exception) {
            GitHubResponse.Error("Error: ${e.message}")
        }
    }

}