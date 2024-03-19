package com.example.githubapisample.data

import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubApiService {

    // 定義 GET 請求方法，指定網址以及查詢參數
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): SearchResult

}