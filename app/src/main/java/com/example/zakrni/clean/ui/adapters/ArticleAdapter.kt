package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.databinding.ItemArticleBinding
import com.example.zakrni.clean.ui.models.PresentationArticle

class ArticleAdapter(
    private val onItemClick: (PresentationArticle) -> Unit = {}
) : ListAdapter<PresentationArticle, ArticleAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArticleViewHolder(
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: PresentationArticle) {
            binding.apply {
                // Bind article title
                tvArticleTitle.text = article.title

                // Bind article description
                tvArticleDescription.text = article.shortdescription ?: ""

                // Set click listener
                root.setOnClickListener {
                    onItemClick(article)
                }
            }
        }
    }

    class ArticleDiffCallback : DiffUtil.ItemCallback<PresentationArticle>() {
        override fun areItemsTheSame(
            oldItem: PresentationArticle,
            newItem: PresentationArticle
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PresentationArticle,
            newItem: PresentationArticle
        ): Boolean {
            return oldItem == newItem
        }
    }
}