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

    /**
     * 驗證:
     * SearchMore -> 在repository資料回來之前再次SearchMore-> 驗證第一次要成功，第二次要被擋掉
     */
    @Test
    fun searchViewModel_searchMoreBeforeResultReturn_shouldCancelNextSearchMore() = runTest {
        searchViewModel.initialSearch("ios") //先偷塞一筆資料
        advanceUntilIdle()
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceTimeBy(FAKE_REPOSITORY_SEARCH_TIME - 200) // 來到一個資料還未返回的時間點
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceUntilIdle()
        // 確認第一次search more結果有回來且加到第二頁且update UIState
        val searchUIStateOne = searchUIStateFlow.first()
        assertEquals(StateType.SUCCESS, searchUIStateOne.stateType)
        assertEquals("ios2", searchUIStateOne.repositories[1].description)
        assertEquals(searchUIStateOne.repositories.size, 2) // 也要確認沒有第三頁(因為應該要被取消)
    }

    /**
     * 驗證:
     * 頁面滿了不再撈資料
     */
    @Test
    fun searchViewModel_searchMoreUntilFull_shouldNotFetchMoreData() = runTest {
        searchViewModel.initialSearch("ios") //先偷塞一筆資料
        // 以下會故意讓第三筆的count超過MAX_COUNT_THRESHOLD
        advanceUntilIdle()
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceUntilIdle()
        // 確認現在有第兩頁資料
        assertEquals(searchViewModel.searchUIStateFlow.first().repositories.size, 2)
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceUntilIdle()
        // 確認現在有第三頁資料
        assertEquals(searchViewModel.searchUIStateFlow.first().repositories.size, 3)
        searchViewModel.searchMore(SearchDirection.BOTTOM)
        advanceUntilIdle()
        // 確認現在有還是只有三頁資料
        assertEquals(searchViewModel.searchUIStateFlow.first().repositories.size, 3)
    }

    /**
     * 驗證:
     * 不斷SearchMore直到超過MAX_COUNT_THRESHOLD，確認第一頁資料會被移除 -> 繼續往下搜 -> 驗證移除頭一筆 -> 網上搜 -> 驗證移除最後一筆
     */
    @Test
    fun searchViewModel_searchMoreUntilFull_shouldRemoveFirstPageData() = runTest {
        val searchUIStateFlow = searchViewModel.searchUIStateFlow
        // 預計MAX_COUNT_THRESHOLD為10000筆，所以第四次會超過，第四次成功時理應自動刪掉第一筆
        searchViewModel.initialSearch("tooMuch") //3000筆
        advanceUntilIdle()
        val searchUIStateOne = searchUIStateFlow.first()
        assertEquals(searchUIStateOne.repositories.size, 3000)
        assertEquals(searchUIStateOne.repositories.first().description, "tooMuch1") //第一筆是第一頁
        assertEquals(searchUIStateOne.repositories.last().description, "tooMuch1") //最後一筆是第一頁
        searchViewModel.searchMore(SearchDirection.BOTTOM) // 6000筆
        advanceUntilIdle()
        val searchUIStateTwo = searchUIStateFlow.first()
        assertEquals(searchUIStateTwo.repositories.size, 6000)
        assertEquals(searchUIStateTwo.repositories.first().description, "tooMuch1") //第一筆是第一頁
        assertEquals(searchUIStateTwo.repositories.last().description, "tooMuch2") //最後一筆是第二頁
        searchViewModel.searchMore(SearchDirection.BOTTOM) // 9000筆
        advanceUntilIdle()
        val searchUIStateThree = searchUIStateFlow.first()
        assertEquals(searchUIStateThree.repositories.size, 9000)
        assertEquals(searchUIStateThree.repositories.first().description, "tooMuch1") //第一筆是第一頁
        assertEquals(searchUIStateThree.repositories.last().description, "tooMuch3") //最後一筆是第三頁
        searchViewModel.searchMore(SearchDirection.BOTTOM) // 90000 + 3000 - 3000 = 9000筆
        advanceUntilIdle()
        // 確認新增最後一筆且第一筆資料被移除
        val searchUIStateFour = searchUIStateFlow.first()
        assertEquals(searchUIStateFour.repositories.size, 9000)
        assertEquals(searchUIStateFour.repositories.first().description, "tooMuch2") //第一筆是第二頁
        assertEquals(searchUIStateFour.repositories.last().description, "tooMuch4") //最後一筆是第四頁
        searchViewModel.searchMore(SearchDirection.BOTTOM) // 90000 + 3000 - 3000 = 9000筆
        advanceUntilIdle()
        // 確認新增最後一筆且最後一筆資料被移除
        val searchUIStateFive = searchUIStateFlow.first()
        assertEquals(searchUIStateFive.repositories.size, 9000)
        assertEquals(searchUIStateFive.repositories.first().description, "tooMuch3") //目前第一筆是第三頁
        assertEquals(searchUIStateFive.repositories.last().description, "tooMuch5") //目前最後一筆是第五頁
        // 往上滑
        searchViewModel.searchMore(SearchDirection.TOP) // 90000 + 3000 - 3000 = 9000筆
        advanceUntilIdle()
        // 確認新增第一筆且最後一筆資料被移除
        val searchUIStateSix = searchUIStateFlow.first()
        assertEquals(searchUIStateSix.repositories.size, 9000)
        assertEquals(searchUIStateSix.repositories.first().description, "tooMuch2") //目前第一筆是第二頁
        assertEquals(searchUIStateSix.repositories.last().description, "tooMuch4") //目前最後一筆是第四頁
    }

}