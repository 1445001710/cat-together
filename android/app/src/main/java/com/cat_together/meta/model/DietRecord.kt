package com.cat_together.meta.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "diet_records")
@Parcelize
data class DietRecord(
    @PrimaryKey
    val id: String = "",
    val catId: String = "",
    val type: Int = TYPE_FOOD,
    val amount: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        const val TYPE_WATER = 1
        const val TYPE_FOOD = 2
        const val TYPE_SNACK = 3
        const val TYPE_TREAT = 4

        fun getTypeName(type: Int): String {
            return when (type) {
                TYPE_WATER -> "喝水"
                TYPE_FOOD -> "猫粮"
                TYPE_SNACK -> "零食"
                TYPE_TREAT -> "猫条"
                else -> "其他"
            }
        }

        fun getTypeIcon(type: Int): String {
            return when (type) {
                TYPE_WATER -> "💧"
                TYPE_FOOD -> "🍖"
                TYPE_SNACK -> "🍪"
                TYPE_TREAT -> "🐟"
                else -> "📝"
            }
        }
    }
}
