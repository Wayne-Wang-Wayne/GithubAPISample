package com.example.githubapisample.utils

import android.util.Log
import com.example.githubapisample.data.remotedata.GitHubResponse
import com.example.githubapisample.ui.SearchDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "FunctionUtil"

class FunctionUtil {

    companion object {

        fun debounce(
            coroutineScope: CoroutineScope,
            milli: Long
        ): (suspend () -> Unit) -> Unit {
            var debounceJob: Job? = null

            return {
                debounceJob?.cancel()
                debounceJob = coroutineScope.launch {
                    delay(milli)
                    it.invoke()
                }
            }
        }

        fun lock(
            coroutineScope: CoroutineScope,
        ): (suspend () -> Unit) -> Job? {
            var isSearching = false
            var job: Job? = null
            return {
                if (!isSearching) {
                    isSearching = true
                    job = coroutineScope.launch {
                        it.invoke()
                        isSearching = false
                        job = null
                    }
                }
                job
            }
        }
    }
}
