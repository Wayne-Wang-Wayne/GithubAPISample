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

    /**
     * 驗證:
     * initial search -> 在debounce時間內再次initial search -> 驗證第一次搜尋被取消，第二次搜尋結果回來且UIState正確更新
     */
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


    /**
     * 驗證:
     * initial search -> 超過debounce時間 -> 在repository資料回來之前再次initial search -> 驗證第一次搜尋被取消，第二次搜尋結果回來且UIState正確更新
     */
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

    /**
     * 驗證:
     * initial search -> 超過debounce時間 -> 驗證第一次搜尋結果回來且UIState正確更新 ->
     * 再次initial search -> 驗證第二次搜尋結果回來且UIState正確更新
     */
    @Test
    fun searchViewModel_initialSearchNoConflict_shouldUpdateCorrectUIStateByOrder() = runTest {
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        searchViewModel.initialSearch("android")
        advanceTimeBy(SEARCH_DEBOUNCE_TIME + makeSureCompleteBufferTime) // 略過debounce時間
        // 確認第一次搜尋有Loading
        val searchUIStateOne = searchUIStateFlow.first()
        assertEquals(StateType.LOADING, searchUIStateOne.stateType)
        advanceTimeBy(FAKE_REPOSITORY_SEARCH_TIME + makeSureCompleteBufferTime) // 略過第一次搜尋時間
        // 確認第一次搜尋結果有回來，要確定是android結果
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(StateType.SUCCESS, searchUIStateTwo.stateType)
        assertEquals(searchUIStateTwo.repositories[0].description, "android1")
        searchViewModel.initialSearch("ios")
        advanceTimeBy(SEARCH_DEBOUNCE_TIME + makeSureCompleteBufferTime) // 略過debounce時間
        // 確認第二次搜尋有Loading
        val searchUIStateThree = searchUIStateFlow.first()
        assertEquals(StateType.LOADING, searchUIStateThree.stateType)
        advanceTimeBy(FAKE_REPOSITORY_SEARCH_TIME + makeSureCompleteBufferTime) // 略過第二次搜尋時間
        // 確認第二次搜尋結果有回來，要確定是ios結果
        val searchUIStateFour = searchUIStateFlow.first()
        assertEquals(StateType.SUCCESS, searchUIStateFour.stateType)
        assertEquals(searchUIStateFour.repositories[0].description, "ios1")
    }

    /**
     * 驗證:
     * initial search 發生錯誤 -> 驗證UIState正確更新
     */
    @Test
    fun searchViewModel_initialSearchFailed_shouldUpdateErrorUIState() = runTest {
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        githubRepository.shouldReturnError = true
        searchViewModel.initialSearch("android")
        advanceTimeBy(SEARCH_DEBOUNCE_TIME + makeSureCompleteBufferTime) // 略過debounce時間
        // 確認第一次搜尋有Loading
        val searchUIStateOne = searchUIStateFlow.first()
        assertEquals(StateType.LOADING, searchUIStateOne.stateType)
        advanceTimeBy(FAKE_REPOSITORY_SEARCH_TIME + makeSureCompleteBufferTime) // 略過第一次搜尋時間
        // 確認UIState正確更新成error
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(StateType.ERROR, searchUIStateTwo.stateType)
        assertEquals(searchUIStateTwo.errorMessage, "Error")
    }

    /**
     * 驗證:
     * initial search -> 未超過debounce時間 ->  SearchMore -> initial search debounce時間到後會取消SearchMore -> 驗證UIState只有顯示initial search結果
     */

    @Test
    fun searchViewModel_searchMoreBeforeInitialSearchDebounce_shouldCancelSearchMore() = runTest {
        searchViewModel.initialSearch("ios") //先偷塞一筆資料
        advanceUntilIdle()
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        searchViewModel.initialSearch("android")
        advanceTimeBy(SEARCH_DEBOUNCE_TIME - 200) // 未超過debounce時間
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceUntilIdle()
        // 確認cancel search more且initial search結果有回來且update UIState，要確定是android第一頁結果
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(StateType.SUCCESS, searchUIStateTwo.stateType)
        assertEquals(searchUIStateTwo.repositories[0].description, "android1")
    }

    /**
     * 驗證:
     * initial search -> 超過debounce時間 -> 在repository資料回來之前SearchMore -> SearchMore會被擋住不會執行 -> 驗證UIState只有顯示initial search結果
     */
    @Test
    fun searchViewModel_searchMoreBeforeInitialSearchDebounceNotOccur_shouldCancelSearchMore() = runTest {
        searchViewModel.initialSearch("ios") //先偷塞一筆資料
        advanceUntilIdle()
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        searchViewModel.initialSearch("android")
        advanceTimeBy(SEARCH_DEBOUNCE_TIME + makeSureCompleteBufferTime) // 略過debounce時間
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceUntilIdle()
        // 確認cancel search more且initial search結果有回來且update UIState，要確定是android第一頁結果
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(StateType.SUCCESS, searchUIStateTwo.stateType)
        assertEquals(searchUIStateTwo.repositories[0].description, "android1")
    }

    /**
     * 驗證:
     * SearchMore -> 在repository資料回來之前進行initial search -> SearchMore會被cancel不會執行 -> 驗證UIState只有顯示initial search結果
     */
    @Test
    fun searchViewModel_searchMoreThenInitialSearchBeforeResultReturn_shouldCancelSearchMore() = runTest {
        searchViewModel.initialSearch("ios") //先偷塞一筆資料
        advanceUntilIdle()
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceTimeBy(FAKE_REPOSITORY_SEARCH_TIME - 200) // 來到一個資料還未返回的時間點
        searchViewModel.initialSearch("android")
        advanceUntilIdle()
        // 確認cancel search more且initial search結果有回來且update UIState，要確定是android第一頁結果
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(StateType.SUCCESS, searchUIStateTwo.stateType)
        assertEquals(searchUIStateTwo.repositories[0].description, "android1")
    }

    /**
     * 驗證:
     * SearchMore -> 在repository資料回來之後正確顯示SearchMore結果 -> initial search -> 驗證UIState正確顯示initial search結果
     */
    @Test
    fun searchViewModel_searchMoreThenInitialSearchBothSuccess_shouldUpdateSearchMoreThenInitialSearch() = runTest {
        searchViewModel.initialSearch("ios") //先偷塞一筆資料
        advanceUntilIdle()
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceUntilIdle()
        // 確認search more結果有回來且update UIState，要確定第二頁ios結果有被加上來
        val searchUIStateOne = searchUIStateFlow.first()
        assertEquals(StateType.SUCCESS, searchUIStateOne.stateType)
        assertEquals("ios2", searchUIStateOne.repositories[1].description)
        searchViewModel.initialSearch("android")
        advanceUntilIdle()
        // 確認initial search結果有回來且update UIState，要確定是android第一頁結果
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(StateType.SUCCESS, searchUIStateTwo.stateType)
        assertEquals(searchUIStateTwo.repositories[0].description, "android1")
    }

}