package com.cat_together.meta.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "media")
@Parcelize
data class Media(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val catId: String = "",
    val type: Int = TYPE_PHOTO,
    val url: String = "",
    val thumbUrl: String = "",
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        const val TYPE_PHOTO = 1
        const val TYPE_VIDEO = 2
    }
}
