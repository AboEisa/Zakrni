package com.zakrni.app.clean.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.zakrni.app.R
import com.zakrni.app.clean.data.models.YouTubeVideo
import com.zakrni.app.clean.ui.utils.LocaleHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.text.format.DateUtils

class VideoAdapter(
    private val onVideoClick: (YouTubeVideo) -> Unit = {}
) : ListAdapter<YouTubeVideo, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.videoCard)
        private val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val playIcon: ImageView = itemView.findViewById(R.id.ivPlayIcon)
        private val duration: TextView = itemView.findViewById(R.id.tvDuration)
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val channelName: TextView = itemView.findViewById(R.id.tvChannelName)
        private val viewCount: TextView = itemView.findViewById(R.id.tvViewCount)
        private val publishedAt: TextView = itemView.findViewById(R.id.tvPublishedAt)

        fun bind(video: YouTubeVideo) {
            val isArabic = LocaleHelper.isArabic(itemView.context)
            // Load thumbnail - try high quality first, fallback to medium quality
            val thumbnailUrl = if (video.thumbnailUrl.isNotEmpty()) {
                video.thumbnailUrl
            } else {
                "https://img.youtube.com/vi/${video.videoId}/hqdefault.jpg"
            }
            
            Glide.with(itemView.context)
                .load(thumbnailUrl)
                .transform(CenterCrop(), RoundedCorners(24))
                .placeholder(R.drawable.placeholder_video)
                .error(R.drawable.placeholder_video)
                .into(thumbnail)

            // Set video info
            title.text = video.title
            channelName.text = video.channelName
            duration.text = if (video.duration.isNotEmpty()) video.duration else ""
            
            // Format view count
            if (video.viewCount.isNotEmpty()) {
                viewCount.text = if (isArabic) "${video.viewCount} مشاهدة" else "${video.viewCount} views"
                viewCount.visibility = View.VISIBLE
            } else {
                viewCount.visibility = View.GONE
            }
            
            // Format published date
            publishedAt.text = formatPublishedDate(video.publishedAt)

            // Click listener
            cardView.setOnClickListener {
                onVideoClick(video)
            }
        }
        
        private fun formatPublishedDate(dateStr: String): String {
            if (dateStr.isEmpty()) return ""
            val isArabic = LocaleHelper.isArabic(itemView.context)
            
            // If already in Arabic relative format, return as-is
            if (dateStr.startsWith("منذ")) {
                return if (isArabic) dateStr else convertArabicRelativeToEnglish(dateStr)
            }
            
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val date = format.parse(dateStr) ?: return dateStr
                val now = Date()
                if (isArabic) {
                    val diffMs = now.time - date.time
                    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
                    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)

                    when {
                        days > 365 -> "منذ ${days / 365} سنة"
                        days > 30 -> "منذ ${days / 30} شهر"
                        days > 7 -> "منذ ${days / 7} أسبوع"
                        days > 0 -> "منذ $days يوم"
                        hours > 0 -> "منذ $hours ساعة"
                        minutes > 0 -> "منذ $minutes دقيقة"
                        else -> "الآن"
                    }
                } else {
                    DateUtils.getRelativeTimeSpanString(
                        date.time,
                        now.time,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                    ).toString()
                }
            } catch (e: Exception) {
                dateStr
            }
        }

        private fun convertArabicRelativeToEnglish(text: String): String {
            if (text.contains("الآن")) return "Just now"

            val number = Regex("""\d+""").find(text)?.value?.toIntOrNull()
            return when {
                text.contains("سنتين") -> "2 years ago"
                text.contains("سنة") -> "${number ?: 1} year${if ((number ?: 1) == 1) "" else "s"} ago"
                text.contains("شهرين") -> "2 months ago"
                text.contains("شهر") -> "${number ?: 1} month${if ((number ?: 1) == 1) "" else "s"} ago"
                text.contains("أسبوعين") -> "2 weeks ago"
                text.contains("أسبوع") -> "${number ?: 1} week${if ((number ?: 1) == 1) "" else "s"} ago"
                text.contains("يومين") -> "2 days ago"
                text.contains("يوم") -> "${number ?: 1} day${if ((number ?: 1) == 1) "" else "s"} ago"
                text.contains("ساعتين") -> "2 hours ago"
                text.contains("ساعة") -> "${number ?: 1} hour${if ((number ?: 1) == 1) "" else "s"} ago"
                text.contains("دقيقتين") -> "2 minutes ago"
                text.contains("دقيقة") -> "${number ?: 1} minute${if ((number ?: 1) == 1) "" else "s"} ago"
                else -> text
            }
        }
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<YouTubeVideo>() {
        override fun areItemsTheSame(oldItem: YouTubeVideo, newItem: YouTubeVideo): Boolean {
            return oldItem.videoId == newItem.videoId
        }

        override fun areContentsTheSame(oldItem: YouTubeVideo, newItem: YouTubeVideo): Boolean {
            return oldItem == newItem
        }
    }
}
