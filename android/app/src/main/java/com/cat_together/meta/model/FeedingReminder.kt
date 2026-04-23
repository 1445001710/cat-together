package com.cat_together.meta.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "feeding_reminders")
@Parcelize
data class FeedingReminder(
    @PrimaryKey
    val id: String = "",
    val catId: String = "",
    val type: Int = DietRecord.TYPE_FOOD,
    val time: String = "08:00",
    val repeatRule: String = "daily",
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        const val REPEAT_DAILY = "daily"
        const val REPEAT_WEEKLY = "weekly"
        const val REPEAT_MONTHLY = "monthly"
    }
}
