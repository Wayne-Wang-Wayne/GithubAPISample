package com.example.githubapisample.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.githubapisample.data.remotedata.GithubRepositoryImpl
import com.example.githubapisample.data.remotedata.RetrofitInstance
import com.example.githubapisample.databinding.FragmentSearchBinding
import com.example.githubapisample.utils.CountConverterImpl
import com.example.githubapisample.utils.GitHubApiDataMapperImpl
import com.example.githubapisample.utils.TimeConverterImpl
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchFragment : Fragment() {

    private val tag = "SearchFragment"

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding
    private val searchViewModel : SearchViewModel by viewModel()
    private val searchEditText get() = binding?.searchEditText
    private val searchRecyclerView get() = binding?.searchRecyclerView
    private val circularProgressBar get() = binding?.circularProgressBar
    private val emptyListTextView get() = binding?.emptyListTextView
    private val searchAdapter: SearchListAdapter by lazy { SearchListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenDataStream()
        setupView()
    }

    private fun setupView() {
        searchEditText?.addTextChangedListener(textWatcher)
        searchRecyclerView?.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(scrollListener)
            addOnItemTouchListener(touchListener)
        }
    }

    private fun listenDataStream() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchUIStateFlow.collect { state ->
                    when (state.stateType) {
                        StateType.LOADING -> {
                            circularProgressBar?.visibility = View.VISIBLE
                            emptyListTextView?.visibility = View.GONE
                        }
                        StateType.SUCCESS -> {
                            circularProgressBar?.visibility = View.GONE
                            if (state.repositories.isNotEmpty()) {
                                searchRecyclerView?.visibility = View.VISIBLE
                                emptyListTextView?.visibility = View.GONE
                                searchAdapter.submitList(state.repositories)
                            } else {
                                searchRecyclerView?.visibility = View.GONE
                                emptyListTextView?.visibility = View.VISIBLE
                            }
                        }
                        StateType.ERROR -> {
                            Toast.makeText(context, "有錯誤，請稍後再試:" + state.errorMessage, Toast.LENGTH_LONG).show()
                            circularProgressBar?.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            // 如果距離頂部小於等於 threshold，則call searchMore(TOP)
            if (firstVisibleItemPosition <= SCROLLING_THRESHOLD) {
                searchViewModel.searchMore(SearchDirection.TOP)
            }

            // 如果距離底部小於等於 threshold，則call searchMore(BOTTOM)
            if (totalItemCount - visibleItemCount <= firstVisibleItemPosition + SCROLLING_THRESHOLD) {
                searchViewModel.searchMore(SearchDirection.BOTTOM)
            }
        }
    }

    private val touchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            if (e.action == MotionEvent.ACTION_DOWN) {
                Log.d(tag, "onInterceptTouchEvent: ACTION_DOWN")
                hideKeyBoard()
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        }
    }

    private fun hideKeyBoard() {
        try {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val focusedView = activity?.currentFocus
            if (focusedView != null) {
                imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            searchViewModel.initialSearch(s.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchEditText?.removeTextChangedListener(textWatcher)
        searchRecyclerView?.removeOnScrollListener(scrollListener)
        searchRecyclerView?.removeOnItemTouchListener(touchListener)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment()
    }
}