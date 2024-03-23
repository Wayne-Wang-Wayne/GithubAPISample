package com.example.githubapisample.data

import com.example.githubapisample.fakeclass.FakeGitHubApiDataMapper
import com.example.githubapisample.fakeclass.FakeGitHubApiService
import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.data.remotedata.GithubRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GitHubRepositoryImplTest {

    private lateinit var gitHubRepositoryImpl: GithubRepositoryImpl

    private lateinit var fakeGitHubApiService: FakeGitHubApiService

    private lateinit var fakeGitHubApiDataMapper: FakeGitHubApiDataMapper

    @OptIn(ExperimentalCoroutinesApi::class)
    private var testDispatcher = UnconfinedTestDispatcher()
    private var testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        fakeGitHubApiService = FakeGitHubApiService()
        fakeGitHubApiDataMapper = FakeGitHubApiDataMapper()
        gitHubRepositoryImpl = GithubRepositoryImpl(fakeGitHubApiService, fakeGitHubApiDataMapper, testDispatcher)
    }
    @Test
    fun gitHubRepositoryImplTest_searchRepositoriesSuccess_shouldGetSuccessGitHubResponse() = testScope.runTest {
        // When
        val result = gitHubRepositoryImpl.searchRepositories("query", 1, 1)
        // Then
        Assert.assertTrue(result is GitHubResponse.Success)
        val repoDataList = (result as GitHubResponse.Success).repoResult.repoDataList
        assertEquals(repoDataList[0].id, 1L)
        assertEquals(repoDataList[1].id, 2L)
        assertEquals(repoDataList[2].id, 3L)
    }
    @Test
    fun gitHubRepositoryImplTest_searchRepositoriesFailed_shouldGetErrorGitHubResponse() = testScope.runTest {
        // Given
        fakeGitHubApiService.forceError = true
        // When
        val result = gitHubRepositoryImpl.searchRepositories("query", 1, 1)
        // Then
        Assert.assertTrue(result is GitHubResponse.Error)
        assertEquals((result as GitHubResponse.Error).message, "Failed to fetch data: ${fakeGitHubApiService.errorCode}")
    }
    @Test
    fun gitHubRepositoryImplTest_searchRepositoriesException_shouldGetErrorGitHubResponse() = testScope.runTest {
        // Given
        fakeGitHubApiService.forceException = true
        // When
        val result = gitHubRepositoryImpl.searchRepositories("query", 1, 1)
        // Then
        Assert.assertTrue(result is GitHubResponse.Error)
        assertEquals((result as GitHubResponse.Error).message, "Error: ${fakeGitHubApiService.exceptionErrorMessage}")
    }

    @Test
    fun gitHubRepositoryImplTest_searchRepositoriesNullBody_shouldGetErrorGitHubResponse() = testScope.runTest {
        // Given
        fakeGitHubApiService.forceNoBody = true
        // When
        val result = gitHubRepositoryImpl.searchRepositories("query", 1, 1)
        // Then
        Assert.assertTrue(result is GitHubResponse.Error)
        assertEquals((result as GitHubResponse.Error).message, "Failed to fetch data: response body is null")
    }

    @Test
    fun gitHubRepositoryImplTest_searchRepositoriesCancelException_shouldGetCancelGitHubResponse() = testScope.runTest {
        // Given
        fakeGitHubApiService.forceCancelException = true
        // When
        val result = gitHubRepositoryImpl.searchRepositories("query", 1, 1)
        // Then
        Assert.assertTrue(result is GitHubResponse.Cancel)
    }

}