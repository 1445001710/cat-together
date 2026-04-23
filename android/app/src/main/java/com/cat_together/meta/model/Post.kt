package com.cat_together.meta.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Post(
    val id: String = "",
    val userId: String = "",
    val catId: String = "",
    val content: String = "",
    val mediaIds: List<String> = emptyList(),
    val mediaUrls: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val user: User? = null,
    val cat: Cat? = null
) : Parcelable {
    val createTimeAgo: String
        get() {
            val diff = System.currentTimeMillis() - createdAt
            val minutes = diff / (1000 * 60)
            val hours = diff / (1000 * 60 * 60)
            val days = diff / (1000 * 60 * 60 * 24)

            return when {
                minutes < 1 -> "刚刚"
                minutes < 60 -> "${minutes}分钟前"
                hours < 24 -> "${hours}小时前"
                days < 30 -> "${days}天前"
                else -> "${days / 30}个月前"
            }
        }
}
