package com.cat_together.meta.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "cats")
@Parcelize
data class Cat(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val breed: String = "",
    val gender: Int = 0,
    val birthday: Long = 0L,
    val color: String = "",
    val avatar: String? = null,
    val weight: Float = 0f,
    val height: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    val ageInMonths: Int
        get() {
            if (birthday == 0L) return 0
            val diff = System.currentTimeMillis() - birthday
            return (diff / (1000L * 60 * 60 * 24 * 30)).toInt()
        }

    val ageInYears: Int
        get() = ageInMonths / 12

    val ageMonthsRemainder: Int
        get() = ageInMonths % 12

    val ageString: String
        get() {
            if (ageInMonths == 0) return "未知"
            return if (ageInYears > 0) {
                if (ageMonthsRemainder > 0) "${ageInYears}岁${ageMonthsRemainder}个月"
                else "${ageInYears}岁"
            } else {
                "${ageInMonths}个月"
            }
        }

    companion object {
        const val GENDER_UNKNOWN = 0
        const val GENDER_MALE = 1
        const val GENDER_FEMALE = 2

        fun getGenderName(gender: Int): String {
            return when (gender) {
                GENDER_MALE -> "公"
                GENDER_FEMALE -> "母"
                else -> "未知"
            }
        }
    }
}
