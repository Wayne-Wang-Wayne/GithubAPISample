package com.example.githubapisample.ui

import com.example.githubapisample.data.remotedata.RepoData

data class SearchUIState(
    val stateType: StateType = StateType.LOADING,
    val errorMessage: String = "",
    val searchQuery: String = "",
    val repositories: List<RepoData> = emptyList()
)

enum class StateType {
    LOADING,
    ERROR,
    SUCCESS
}
