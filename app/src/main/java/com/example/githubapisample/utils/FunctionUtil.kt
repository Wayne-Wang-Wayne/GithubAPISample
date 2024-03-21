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

        fun <T> debounce(
            coroutineScope: CoroutineScope,
            runnable: suspend (T) -> Unit,
            milli: Long
        ): (T) -> Unit {
            var debounceJob: Job? = null

            return {
                debounceJob?.cancel()
                Log.d(TAG, "debounce $it")
                debounceJob = coroutineScope.launch {
                    delay(milli)
                    runnable(it)
                }
            }
        }

        fun <T> lock(
            coroutineScope: CoroutineScope,
            runnable: suspend (T) -> Unit
        ): (T) -> Job? {
            var isSearching = false
            var job: Job? = null
            return { param ->
                if (!isSearching) {
                    isSearching = true
                    job = coroutineScope.launch {
                        runnable(param)
                        isSearching = false
                        job = null
                    }
                }
                job
            }
        }
    }
}
