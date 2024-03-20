package com.example.githubapisample.utils


import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.data.remotedata.Owner
import com.example.githubapisample.data.remotedata.Repository
import com.example.githubapisample.data.remotedata.SearchResult
import com.example.githubapisample.fakeclass.FakeCountConverter
import com.example.githubapisample.fakeclass.FakeTimeConverter
import org.junit.Before
import org.junit.Test

class GitHubApiDataMapperImplTest {

    private lateinit var gitHubApiDataMapperImpl: GitHubApiDataMapperImpl

    private lateinit var fakeTimeConverter: FakeTimeConverter

    private lateinit var fakeCountConverter: FakeCountConverter

    private val fakeSuccessData = SearchResult(300, false, listOf(
        Repository(
            id = 1L,
            fullName = "name1",
            description = "description1",
            updatedAt = "2021-03-07",
            stargazersCount = 100,
            language = "Kotlin",
            owner = Owner(avatarUrl = "avatarUrl1"),
        ),
        Repository(),
    ))

    @Before
    fun setUp() {
        fakeTimeConverter = FakeTimeConverter()
        fakeCountConverter = FakeCountConverter()
        gitHubApiDataMapperImpl = GitHubApiDataMapperImpl(fakeTimeConverter, fakeCountConverter)
    }

    @Test
    fun gitHubApiDataMapperImplTest_mapToRepoDataList_shouldGetMappedRepoDataList() {
        // When
        val result = gitHubApiDataMapperImpl.toSuccessGitHubResponse(fakeSuccessData)
        // Then
        assert(result is GitHubResponse.Success)
        if (result is GitHubResponse.Success) {
            val repoDataList = result.repoResult.repoDataList
            assert(repoDataList.isNotEmpty())
            assert(repoDataList.size == 2)
            assert(result.repoResult.totalCount == 300)
            // 驗證第一個item
            assert(repoDataList[0].id == 1L)
            assert(repoDataList[0].fullName == "name1")
            assert(repoDataList[0].description == "description1")
            assert(repoDataList[0].updatedAt == fakeTimeConverter.fakeTime)
            assert(repoDataList[0].stargazersCountS == fakeCountConverter.fakeCount)
            assert(repoDataList[0].language == "Kotlin")
            assert(repoDataList[0].avatarUrl == "avatarUrl1")
            // 驗證第二個item
            assert(repoDataList[1].id == 0L)
            assert(repoDataList[1].fullName == "")
            assert(repoDataList[1].description == "")
            assert(repoDataList[1].updatedAt == fakeTimeConverter.fakeTime)
            assert(repoDataList[1].stargazersCountS == fakeCountConverter.fakeCount)
            assert(repoDataList[1].language == "")
            assert(repoDataList[1].avatarUrl == "")
        }
    }

}