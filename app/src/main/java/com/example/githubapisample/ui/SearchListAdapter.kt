package com.example.githubapisample.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.githubapisample.R
import com.example.githubapisample.data.remotedata.RepoData
import com.example.githubapisample.databinding.ItemSearchBinding

class SearchListAdapter : ListAdapter<RepoData, SearchListAdapter.ViewHolder>(RepoDataDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(repoData: RepoData) {
            binding.apply {
                avatarImageView.load(repoData.avatarUrl)
                titleTextView.text = repoData.fullName
                contentTextView.text = repoData.description
                languageTextView.text = repoData.language
                starCountTextView.text = repoData.stargazersCount.toString()
                timeTextView.text = itemView.context.getString(R.string.update_time_text, repoData.updatedAt)
            }
        }
    }
}

class RepoDataDiffCallback : DiffUtil.ItemCallback<RepoData>() {
    override fun areItemsTheSame(oldItem: RepoData, newItem: RepoData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RepoData, newItem: RepoData): Boolean {
        return oldItem.id == newItem.id
    }
}