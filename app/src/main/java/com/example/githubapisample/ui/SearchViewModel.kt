package com.example.githubapisample.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubapisample.data.GithubRepository
import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.data.remotedata.RepoData
import com.example.githubapisample.utils.FunctionUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.LinkedList

class SearchViewModel(
    private val githubRepository: GithubRepository,
) : ViewModel() {

    private val tag = "SearchViewModel"

    private val _searchUIStateFlow = MutableStateFlow(SearchUIState())

    val searchUIStateFlow: StateFlow<SearchUIState> = _searchUIStateFlow.asStateFlow()

    /**
     * 使用LinkedList，因remove first和last的時間複雜度為O(1)
     */
    private val searchPages: LinkedList<Pair<Int, List<RepoData>>> = LinkedList()

    /**
     * 使用Generator方式管理Task，避免衝突
     * 使用方式大致上為，每次打字都會產生一個新的Generator(因狀態會重設)，並使用next去撈資料
     * SearchMore會使用當前這個Generator去call next使用(因狀態會持續)
     */
    private var gitHubResponseGenerator: GitHubResponseGenerator? = null


    /**
     * 使用已經實作好的debounce去包裝(避免過多的搜尋)，使用者只須思考自己想要的邏輯
     */
    val debounce = FunctionUtil.debounce(viewModelScope, SEARCH_DEBOUNCE_TIME)


    /**
     * 變換關鍵字的初始搜尋
     */
    fun initialSearch(searchString: String) {
        debounce debounce@{
            if (searchString == "") return@debounce
            gitHubResponseGenerator?.destroy() // 搜尋關鍵字為主要的搜尋條件，所以每次搜尋都會確保其他task被cancel
            updateLoadingUIState()
            gitHubResponseGenerator = GitHubResponseGenerator(searchString) // 產生新的Generator
            gitHubResponseGenerator?.next(SearchDirection.NONE, { _, response ->
                searchPages.add(1 to response.repoResult.repoDataList)
            }) // call next撈一筆資料
        }
    }


    /**
     * 搜尋更多，貫徹infinite scroll的概念，會移除上下方離太遠的資料，就算滑了一兆筆資料也不會OOM
     * remove first和last的時間複雜度為O(1)，十分有效率
     */
    fun searchMore(searchDirection: SearchDirection) {
        var onSuccess: (Int, GitHubResponse.Success) -> Unit = { _, _ -> }
        if (searchDirection == SearchDirection.TOP) {
            onSuccess = { page, response ->
                searchPages.addFirst(page to response.repoResult.repoDataList)
                if (MAX_COUNT_THRESHOLD < searchPages.sumOf { it.second.size }) {
                    searchPages.removeLast()
                }
            }
        } else if (searchDirection == SearchDirection.BOTTOM) {
            onSuccess = { lastPage, response ->
                Log.d(tag, "lastPage $lastPage")
                searchPages.add(lastPage to response.repoResult.repoDataList)
                if (MAX_COUNT_THRESHOLD < searchPages.sumOf { it.second.size }) {
                    searchPages.removeFirst()
                }
            }
        }
        gitHubResponseGenerator?.next(searchDirection, onSuccess)
    }


    private fun updateSuccessState(validList: List<RepoData>) {
        _searchUIStateFlow.update {
            it.copy(
                stateType = StateType.SUCCESS,
                repositories = validList
            )
        }
    }

    private fun updateLoadingUIState() {
        _searchUIStateFlow.update {
            it.copy(
                stateType = StateType.LOADING
            )
        }
    }


    private fun updateErrorUIState(message: String) {
        _searchUIStateFlow.update {
            it.copy(
                stateType = StateType.ERROR,
                errorMessage = message
            )
        }
    }

    inner class GitHubResponseGenerator(private val input: String) {
        init {
            // 被重新創建時會清掉舊的資料
            searchPages.clear()
        }

        private var isMaxPageReached = false
        private var isSearching = false

        private var searchJob: Job? = null

        /**
         * 使用已經實作好的lock去包裝，如當前還在處理，會無視新的搜尋
         */
        private val lock = FunctionUtil.lock(viewModelScope)

        fun next(
            searchDirection: SearchDirection,
            onSuccess: (Int, GitHubResponse.Success) -> Unit = { _, _ -> },
            onError: (GitHubResponse.Error) -> Unit = {}
        ) {
            val nextPage = when (searchDirection) {
                SearchDirection.TOP -> {
                    safeSearchMoreTop()
                }

                SearchDirection.BOTTOM -> {
                    safeSearchMoreBottom()
                }

                SearchDirection.NONE -> {
                    1
                }
            }

            Log.d("safeSearchMoreBottom", "${searchDirection.name} $nextPage $isSearching")

            if (nextPage == -1) return // 代表我當前算出來需要撈的頁數是不合理的，所以不撈

            searchJob = lock {
                Log.d(tag, "searching $input ${Thread.currentThread()}")
                search(input, PER_PAGE_COUNT, nextPage, {
                    onSuccess(nextPage, it)
                }, onError)
                Log.d(tag, "searching ended")
            }

        }

        fun destroy() {
            searchJob?.cancel()
        }

        private fun safeSearchMoreTop(): Int {
            val firstPage = searchPages.firstOrNull()?.first ?: 1
            if (firstPage <= 1) {
                return -1
            }
            return firstPage - 1
        }

        private fun safeSearchMoreBottom(): Int {
            val lastPage = searchPages.lastOrNull()?.first ?: -1
            if (isMaxPageReached || lastPage == -1) {
                return -1
            }
            return (lastPage + 1)
        }

        private fun modifyIsMaxFlag(maxCount: Int) {
            isHasMoreData(maxCount).let { isHasMore ->
                isMaxPageReached = !isHasMore
            }
        }

        private fun isHasMoreData(totalCount: Int): Boolean {
            val lastPage = searchPages.lastOrNull()?.first ?: -1
            return lastPage * PER_PAGE_COUNT < totalCount
        }

        /**
         * 核心搜尋邏輯
         */
        private suspend fun search(
            query: String, perPage: Int, page: Int,
            onSuccess: (GitHubResponse.Success) -> Unit,
            onError: (GitHubResponse.Error) -> Unit = {}
        ) {

            Log.d(tag, "searching $query $perPage $page")
            val response = githubRepository.searchRepositories(
                query,
                perPage,
                page
            )

            when (response) {
                is GitHubResponse.Success -> {
                    onSuccess(response)
                    Log.d(tag, "searchPages Bottom: ${searchPages.map { it.first }}")
                    updateSuccessState(searchPages.flatMap { repositories -> repositories.second })
                    modifyIsMaxFlag(response.repoResult.totalCount)
                }

                is GitHubResponse.Error -> {
                    onError(response)
                    Log.d(tag, response.message)
                    updateErrorUIState(response.message)
                    isMaxPageReached = false
                }

                is GitHubResponse.Cancel -> {

                }
            }
        }
    }
}

private const val MAX_COUNT_THRESHOLD = 10000

private const val PER_PAGE_COUNT = 100

private const val SEARCH_DEBOUNCE_TIME = 500L

const val SCROLLING_THRESHOLD = 200

enum class SearchDirection {
    TOP,
    BOTTOM,
    NONE
}