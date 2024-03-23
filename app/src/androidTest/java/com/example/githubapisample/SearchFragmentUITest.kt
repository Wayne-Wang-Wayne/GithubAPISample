package com.example.githubapisample

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.githubapisample.data.remotedata.RepoData
import com.example.githubapisample.rules.MainCoroutineRule
import com.example.githubapisample.tools.KoinTestRule
import com.example.githubapisample.ui.SearchDirection
import com.example.githubapisample.ui.SearchFragment
import com.example.githubapisample.ui.SearchUIState
import com.example.githubapisample.ui.SearchViewModel
import com.example.githubapisample.ui.StateType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class SearchFragmentUITest {

    private val tag = "MySearchFragmentUITest"

    private val instrumentedTestModule = module {
        single<SearchViewModel> {
            mockk<SearchViewModel>(relaxed = true)
        }
    }

    private val recyclerView get() = onView(withId(R.id.searchRecyclerView))

    private val editText get() = onView(withId(R.id.searchEditText))

    private val progressBar get() = onView(withId(R.id.circularProgressBar))

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(instrumentedTestModule)
    )

    @Before
    fun setup() {
        initSearchFragment()
    }

    /**
     * 驗證：當Loading時，應該顯示LoadingView
     */
    @Test
    fun searchFragmentUITest_ifLoading_shouldShowLoadingView() {
        editText.perform(typeText("a"))
        progressBar.check(matches(isDisplayed()))
    }

    /**
     * 驗證：當輸入文字時，應該顯示鍵盤並且文字被設定到EditText中
     */
    @Test
    fun searchFragmentUITest_typeInWords_shouldCheckKeyboardOpenedAndTextSet() {
        editText.perform(typeText("a"))
        // 驗證輸入的文字是否已設置到 EditText 中
        editText.check(matches(withText("a")))
        // 驗證鍵盤是否已打開
        assert(isKeyboardOpenedShellCheck())
    }

    /**
     * 驗證：拿到正確結果時RecyclerView應該顯示，ProgressBar需消失，且卡片畫面需正確顯示
     */
    @Test
    fun searchFragmentUITest_onSuccessDataGet_shouldHideProgressBarAndShowCorrectCard() {
        editText.perform(typeText("an"))
        progressBar.check(matches(not(isDisplayed())))
        recyclerView.check(matches(isDisplayed()))
        // 驗證卡片畫面是否正確顯示
        recyclerView.perform(
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(10)
        )
        recyclerView.check(matches(withViewAtPosition(10, hasDescendant(withText("fullName10")))))
    }

    private fun createFakeData(count: Int): List<RepoData> {
        return (0 until count).map {
            RepoData(
                id = it.toLong(),
                fullName = "fullName$it",
                description = "description$it",
                updatedAt = "updatedAt$it",
                stargazersCountS = "stargazersCountS$it",
                language = "language$it",
                avatarUrl = "https://randomuser.me/api/portraits/men/${it + 1}.jpg",
                repoUrl = "repoUrl$it"
            )
        }
    }

    private fun isKeyboardOpenedShellCheck(): Boolean {
        val checkKeyboardCmd = "dumpsys input_method | grep mInputShown"

        try {
            return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .executeShellCommand(checkKeyboardCmd).contains("mInputShown=true")
        } catch (e: IOException) {
            throw RuntimeException("Keyboard check failed", e)
        }
    }

    private fun initSearchFragment() {
        val searchUIStateFlow = MutableStateFlow(SearchUIState())
        val scenario =
            launchFragmentInContainer<SearchFragment>(
                null,
                R.style.Base_Theme_GithubAPISample,
                Lifecycle.State.CREATED
            )

        scenario.withFragment {
            every { searchViewModel.searchUIStateFlow } returns searchUIStateFlow
            every { searchViewModel.initialSearch("a") } answers {
                searchUIStateFlow.value = searchUIStateFlow.value.copy(
                    stateType = StateType.LOADING
                )
            }
            every { searchViewModel.initialSearch("an") } answers {
                searchUIStateFlow.value = searchUIStateFlow.value.copy(
                    stateType = StateType.SUCCESS,
                    repositories = createFakeData(20)
                )
            }
            every { searchViewModel.searchMore(SearchDirection.BOTTOM) } answers {
                searchUIStateFlow.value = searchUIStateFlow.value.copy(
                    stateType = StateType.SUCCESS,
                    repositories = createFakeData(40)
                )
            }
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    private fun withViewAtPosition(position: Int, itemMatcher: Matcher<View?>): BoundedMatcher<View?, RecyclerView> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description?) {
                itemMatcher.describeTo(description)
            }

            override fun matchesSafely(recyclerView: RecyclerView): Boolean {
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                return viewHolder != null && itemMatcher.matches(viewHolder.itemView)
            }
        }
    }

}