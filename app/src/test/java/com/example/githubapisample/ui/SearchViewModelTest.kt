package com.example.githubapisample.ui

import android.util.Log
import com.example.githubapisample.fakeclass.FAKE_REPOSITORY_SEARCH_TIME
import com.example.githubapisample.fakeclass.FakeGithubRepository
import com.example.githubapisample.rules.MainCoroutineRule
import io.mockk.every
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var githubRepository: FakeGithubRepository

    private val makeSureCompleteBufferTime = 100L

    @Before
    fun setupSearchViewModel() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        githubRepository = FakeGithubRepository()
        searchViewModel = SearchViewModel(githubRepository)
    }

    @Test
    fun searchViewModel_initialSearchDebounceOccur_shouldCancelPreviousAndStartNext() = runTest {
        val diffTime = 300L
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        searchViewModel.initialSearch("android")
        advanceTimeBy(diffTime) // 錯開時間進行驗證
        searchViewModel.initialSearch("ios")
        advanceTimeBy(SEARCH_DEBOUNCE_TIME + makeSureCompleteBufferTime)
        // 確認debounce後有Loading
        val searchUIStateOne = searchUIStateFlow.first()
        assertEquals(StateType.LOADING, searchUIStateOne.stateType)
        advanceTimeBy(FAKE_REPOSITORY_SEARCH_TIME - diffTime + makeSureCompleteBufferTime)
        // 要確認第一次的搜尋被取消沒有回來
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(StateType.LOADING, searchUIStateTwo.stateType)
        // 略過所有異步直到完成
        advanceUntilIdle()
        // 確認第二次的搜尋結果有回來，要確定是ios結果
        val searchUIStateThree = searchUIStateFlow.first()
        assertEquals(searchUIStateThree.stateType, StateType.SUCCESS)
        assertEquals(searchUIStateThree.repositories[0].description, "ios1")
    }

    @Test
    fun searchViewModel_initialSearchDebounceNotOccurButStartBeforeNextResultReturn_shouldCancelPreviousAndOnlyUpdateNextResult() = runTest {
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        searchViewModel.initialSearch("android")
        advanceTimeBy(SEARCH_DEBOUNCE_TIME + makeSureCompleteBufferTime) // 略過debounce時間
        // 確認第一次搜尋有Loading
        val searchUIStateOne = searchUIStateFlow.first()
        assertEquals(StateType.LOADING, searchUIStateOne.stateType)
        searchViewModel.initialSearch("ios")
        advanceTimeBy(FAKE_REPOSITORY_SEARCH_TIME + makeSureCompleteBufferTime) // 時間來到第一次結果回來的時間
        // 確認第一次結果沒有回來
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(StateType.LOADING, searchUIStateTwo.stateType)
        // 略過所有異步直到完成
        advanceUntilIdle()
        // 確認第二次的搜尋結果有回來，要確定是ios結果
        val searchUIStateThree = searchUIStateFlow.first()
        assertEquals(searchUIStateThree.stateType, StateType.SUCCESS)
        assertEquals(searchUIStateThree.repositories[0].description, "ios1")
    }

}