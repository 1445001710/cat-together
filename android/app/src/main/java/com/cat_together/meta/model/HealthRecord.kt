package com.cat_together.meta.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "health_records")
@Parcelize
data class HealthRecord(
    @PrimaryKey
    val id: String = "",
    val catId: String = "",
    val recordType: Int = TYPE_WEIGHT,
    val value: Float = 0f,
    val recordDate: Long = System.currentTimeMillis(),
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        const val TYPE_WEIGHT = 1
        const val TYPE_HEIGHT = 2
        const val TYPE_VACCINE = 3
        const val TYPE_DEWORMING = 4

        fun getTypeName(type: Int): String {
            return when (type) {
                TYPE_WEIGHT -> "体重"
                TYPE_HEIGHT -> "身高"
                TYPE_VACCINE -> "疫苗"
                TYPE_DEWORMING -> "驱虫"
                else -> "其他"
            }
        }

        fun getUnit(type: Int): String {
            return when (type) {
                TYPE_WEIGHT -> "kg"
                TYPE_HEIGHT -> "cm"
                else -> ""
            }
        }
    }
}
