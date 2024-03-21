package com.example.githubapisample.utils

import android.util.Log
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
    }
}
