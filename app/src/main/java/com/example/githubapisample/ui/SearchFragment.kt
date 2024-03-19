package com.example.githubapisample.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.githubapisample.data.remotedata.GithubRepositoryImpl
import com.example.githubapisample.data.remotedata.RetrofitInstance
import com.example.githubapisample.databinding.FragmentSearchBinding
import com.example.githubapisample.utils.GitHubApiDataMapperImpl
import com.example.githubapisample.utils.TimeConverter
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding
    private val viewModel by viewModels<SearchViewModel> {
        SearchViewModelFactory(GithubRepositoryImpl(
            gitHubApiService = RetrofitInstance.apiService,
            gitHubApiDataMapper = GitHubApiDataMapperImpl(TimeConverter())
        ))
    }
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
        }
    }

    private fun listenDataStream() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchUIStateFlow.collect { state ->
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
                            Toast.makeText(context, "有錯誤:" + state.errorMessage, Toast.LENGTH_LONG).show()
                            circularProgressBar?.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            // TODO: Implement search
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchEditText?.removeTextChangedListener(textWatcher)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment()
    }
}