package com.example.githubapisample.fakeclass

import com.example.githubapisample.data.remotedata.GitHubApiService
import com.example.githubapisample.data.remotedata.Repository
import com.example.githubapisample.data.remotedata.SearchResult
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

class FakeGitHubApiService : GitHubApiService {

    var forceException = false

    var forceCancelException = false

    var forceError = false

    var forceNoBody = false

    val exceptionErrorMessage = "Get repositories from api error!"

    val errorCode = 404

    private val fakeSuccessData = SearchResult(300, false, listOf(
        Repository(id = 1L),
        Repository(id = 2L),
        Repository(id = 3L),
    ))

    override suspend fun searchRepositories(
        query: String,
        perPage: Int,
        page: Int
    ): Response<SearchResult> {
        if (forceCancelException) throw CancellationException("Request is cancelled")
        else if (forceException) throw java.lang.Exception(exceptionErrorMessage)
        return if(forceError) Response.error(errorCode, "It is an API error".toResponseBody())
        else if(forceNoBody) Response.success(null)
        else Response.success(fakeSuccessData)
    }
}