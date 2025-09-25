// ui/adapters/ArticlesAdapter.kt
package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zakrni.R
import com.example.zakrni.databinding.ItemArticleBinding
import com.example.zakrni.clean.ui.models.PresentationArticle

class ArticlesAdapter(
    private val onItemClick: (PresentationArticle) -> Unit
) : ListAdapter<PresentationArticle, ArticlesAdapter.ArticleViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArticleViewHolder(
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(article: PresentationArticle) {
            binding.apply {
                articleTitle.text = article.title
                articleDescription.text = article.content.take(100) + "..."
                articleDate.text = article.formattedDate
                articleViews.text = "• ${article.viewsCount} مشاهدة"

                Glide.with(articleImage)
                    .load(article.imageUrl)
                    .placeholder(R.drawable.article_placeholder)
                    .into(articleImage)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PresentationArticle>() {
        override fun areItemsTheSame(old: PresentationArticle, new: PresentationArticle) =
            old.id == new.id
        override fun areContentsTheSame(old: PresentationArticle, new: PresentationArticle) =
            old == new
    }
}