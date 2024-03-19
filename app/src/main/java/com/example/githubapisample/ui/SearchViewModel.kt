package com.example.githubapisample.ui

import androidx.lifecycle.ViewModel
import com.example.githubapisample.data.GithubRepository
import com.example.githubapisample.data.remotedata.RepoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.LinkedList

class SearchViewModel(
    private val githubRepository: GithubRepository,
) : ViewModel() {

    private val _searchUIStateFlow = MutableStateFlow(SearchUIState())

    val searchUIStateFlow: StateFlow<SearchUIState> = _searchUIStateFlow.asStateFlow()

    private val searchPages: LinkedList<Pair<Int, List<RepoData>>> = LinkedList()

    private var isLoadMore = false

    fun initialSearch(s: String) {
        _searchUIStateFlow.update { it.copy(isLoading = false) }
    }

    fun moreSearch(searchDirection: SearchDirection) {
        when (searchDirection) {
            SearchDirection.TOP -> {
                // TODO: Implement top search
            }
            SearchDirection.BOTTOM -> {
                // TODO: Implement bottom search
            }
        }
    }

}

enum class SearchDirection {
    TOP,
    BOTTOM
}