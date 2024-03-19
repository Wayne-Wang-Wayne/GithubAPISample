package com.example.githubapisample.data

import com.example.githubapisample.data.remotedata.GitHubResponse

interface GithubRepository {

    suspend fun searchRepositories(query: String, perPage: Int, page: Int): GitHubResponse

}