package com.example.githubapisample

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.githubapisample.rules.MainCoroutineRule
import com.example.githubapisample.tools.KoinTestRule
import com.example.githubapisample.ui.SearchFragment
import com.example.githubapisample.ui.SearchUIState
import com.example.githubapisample.ui.SearchViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setUp() {
        val searchUIStateFlow = MutableStateFlow(SearchUIState())
        val scenario =
            launchFragmentInContainer<SearchFragment>(
                null,
                R.style.Base_Theme_GithubAPISample,
                Lifecycle.State.CREATED
            )

        scenario.withFragment {
            every { searchViewModel.searchUIStateFlow } returns searchUIStateFlow
            every { searchViewModel.initialSearch("") } returns Unit
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    @Test
    fun testEventFragment() = runTest {
        onView(withId(R.id.searchEditText)).perform(typeText("a"))

        onView(withId(R.id.searchEditText)).perform(typeText("aaaa"))
    }


}