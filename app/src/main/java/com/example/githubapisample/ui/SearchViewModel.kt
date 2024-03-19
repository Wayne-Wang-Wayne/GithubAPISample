package com.example.githubapisample.ui

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

    private val _searchUIStateFlow = MutableStateFlow(SearchUIState())

    val searchUIStateFlow: StateFlow<SearchUIState> = _searchUIStateFlow.asStateFlow()

    private val searchPages: LinkedList<Pair<Int, List<RepoData>>> = LinkedList()

    private var isLoadingMore = false

    private var searchJob: Job? = null

    private var searchQuery: String = ""

    private var isMaxPageReached = false

    fun initialSearch(searchString: String) {
        isMaxPageReached = true
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            searchPages.clear()
            githubRepository.searchRepositories(
                query = searchString,
                perPage = 50,
                page = 1
            ).let { response ->
                when (response) {
                    is GitHubResponse.Success -> {
                        searchPages.add(1 to response.repoResult.repoDataList)
                        _searchUIStateFlow.update {
                            it.copy(
                                stateType = StateType.SUCCESS,
                                repositories = response.repoResult.repoDataList
                            )
                        }
                        isHasMoreData(response.repoResult.totalCount).let { isHasMore ->
                            isMaxPageReached = !isHasMore
                        }
                    }
                    is GitHubResponse.Error -> {
                        _searchUIStateFlow.update {
                            it.copy(
                                stateType = StateType.ERROR,
                                errorMessage = response.message
                            )
                        }
                    }

                }
            }
        }
    }

    suspend fun searchMore(searchDirection: SearchDirection) {
        if (isLoadingMore || isMaxPageReached) return
        when (searchDirection) {
            SearchDirection.TOP -> {
                // TODO: Implement top search
            }
            SearchDirection.BOTTOM -> {
                // TODO: Implement bottom search
            }
        }
    }

    private fun isHasMoreData(totalCount: Int): Boolean {
        return searchPages.sumOf { it.second.size } < totalCount
    }

}

enum class SearchDirection {
    TOP,
    BOTTOM
}

class SearchViewModelFactory(private val githubRepository: GithubRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(githubRepository) as T
}