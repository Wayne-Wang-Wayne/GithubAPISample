package com.example.githubapisample.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.githubapisample.data.GithubRepository
import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.data.remotedata.RepoData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.LinkedList

class SearchViewModel(
    private val githubRepository: GithubRepository,
) : ViewModel() {

    private val tag = "SearchViewModel"

    private val _searchUIStateFlow = MutableStateFlow(SearchUIState())

    val searchUIStateFlow: StateFlow<SearchUIState> = _searchUIStateFlow.asStateFlow()

    // 使用LinkedList，因remove first和last的時間複雜度為O(1)
    private val searchPages: LinkedList<Pair<Int, List<RepoData>>> = LinkedList()

    private var isLoadingMore = false

    private var searchJob: Job? = null

    private var searchQuery: String = ""

    private var isMaxPageReached = false

    fun initialSearch(searchString: String) {
        searchQuery = searchString
        searchJob?.cancel()
        updateLoadingUIState()
        if (searchString.isEmpty()) {
            updateSuccessState(searchPages.firstOrNull()?.second ?: emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_TIME) // debounce
            searchPages.clear()
            githubRepository.searchRepositories(
                query = searchString,
                perPage = PER_PAGE_COUNT,
                page = 1
            ).let { response ->
                when (response) {
                    is GitHubResponse.Success -> {
                        searchPages.add(1 to response.repoResult.repoDataList)
                        updateSuccessState(response.repoResult.repoDataList)
                        modifyIsMaxFlag(response.repoResult.totalCount)
                    }
                    is GitHubResponse.Error -> {
                        updateErrorUIState(response.message)
                        isMaxPageReached = false
                    }
                }
            }
        }
    }

    /**
     * 搜尋更多，貫徹infinite scroll的概念，會移除上下方離太遠的資料，就算滑了一兆筆資料也不會OOM
     * remove first和last的時間複雜度為O(1)，十分有效率
     */
    fun searchMore(searchDirection: SearchDirection) {
        if (isLoadingMore || searchPages.isEmpty()) return
        isLoadingMore = true
        when (searchDirection) {
            SearchDirection.TOP -> {
                searchPages.safeSearchMoreTop { firstPage ->
                    viewModelScope.launch {
                        githubRepository.searchRepositories(
                            query = searchQuery,
                            perPage = PER_PAGE_COUNT,
                            page = firstPage
                        ).let { response ->
                            when (response) {
                                is GitHubResponse.Success -> {
                                    searchPages.addFirst(firstPage to response.repoResult.repoDataList)
                                    if (MAX_COUNT_THRESHOLD < searchPages.sumOf { it.second.size }) {
                                        searchPages.removeLast()
                                    }
                                    updateSuccessState(searchPages.flatMap { repositories -> repositories.second })
                                    Log.d(tag, "searchPages Bottom: ${searchPages.map { it.first }}")
                                    modifyIsMaxFlag(response.repoResult.totalCount)
                                }
                                is GitHubResponse.Error -> {
                                    updateErrorUIState(response.message)
                                }
                            }
                        }
                        isLoadingMore = false
                    }
                }
            }
            SearchDirection.BOTTOM -> {
                safeSearchMoreBottom { lastPage ->
                    viewModelScope.launch {
                        githubRepository.searchRepositories(
                            query = searchQuery,
                            perPage = PER_PAGE_COUNT,
                            page = lastPage
                        ).let { response ->
                            when (response) {
                                is GitHubResponse.Success -> {
                                    searchPages.add(lastPage to response.repoResult.repoDataList)
                                    if (MAX_COUNT_THRESHOLD < searchPages.sumOf { it.second.size }) {
                                        searchPages.removeFirst()
                                    }
                                    updateSuccessState(searchPages.flatMap { repositories -> repositories.second })
                                    modifyIsMaxFlag(response.repoResult.totalCount)
                                    Log.d("SearchViewModel", "searchPages Bottom: ${searchPages.map { it.first }}")
                                }
                                is GitHubResponse.Error -> {
                                    updateErrorUIState(response.message)
                                }
                            }
                        }
                        isLoadingMore = false
                    }
                }
            }
        }
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

    private fun modifyIsMaxFlag(maxCount: Int) {
        isHasMoreData(maxCount).let { isHasMore ->
            isMaxPageReached = !isHasMore
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

    private fun isHasMoreData(totalCount: Int): Boolean {
        val lastPage = searchPages.lastOrNull()?.first ?: -1
        return lastPage * PER_PAGE_COUNT < totalCount
    }

    private fun LinkedList<Pair<Int, List<RepoData>>>.safeSearchMoreTop(callback: (Int) -> Unit) {
        val firstPage = firstOrNull()?.first ?: 1
        if (firstPage <= 1) {
            isLoadingMore = false
            return
        }
        callback(firstPage - 1)
    }

    private fun safeSearchMoreBottom(callback: (Int) -> Unit) {
        val lastPage = searchPages.lastOrNull()?.first ?: -1
        if (isMaxPageReached || lastPage == -1) {
            isLoadingMore = false
            return
        }
        callback(lastPage + 1)
    }

}

private const val MAX_COUNT_THRESHOLD = 10000

private const val PER_PAGE_COUNT = 100

private const val SEARCH_DEBOUNCE_TIME = 500L

const val SCROLLING_THRESHOLD = 40

enum class SearchDirection {
    TOP,
    BOTTOM
}

class SearchViewModelFactory(private val githubRepository: GithubRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(githubRepository) as T
}