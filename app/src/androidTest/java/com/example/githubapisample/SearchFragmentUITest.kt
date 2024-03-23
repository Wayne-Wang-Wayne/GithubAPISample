package com.example.githubapisample

import android.util.Log
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.githubapisample.tools.KoinTestRule
import com.example.githubapisample.ui.SearchFragment
import com.example.githubapisample.ui.SearchViewModel
import com.google.android.material.textfield.TextInputEditText
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class SearchFragmentUITest {

    private val tag = "MySearchFragmentUITest"

    private val instrumentedTestModule = module {
        factory<SearchViewModel> {
            mockk<SearchViewModel>()
        }
    }

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(instrumentedTestModule)
    )

    @Before
    fun setUp() {

    }

    @Test
    fun testEventFragment() {
        Log.d(tag, "Can Log")
        val scenario = launchFragmentInContainer<SearchFragment>(themeResId = R.style.Theme_GithubAPISample)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            val editText = it.view?.findViewById<TextInputEditText>(R.id.searchEditText)
            assert(editText != null)
        }
    }

}