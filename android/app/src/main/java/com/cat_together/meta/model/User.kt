package com.cat_together.meta.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Keep
@Parcelize
data class User(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("username")
    val username: String = "",

    @SerializedName("phone")
    val phone: String = "",

    @SerializedName("nickname")
    val nickname: String = "",

    @SerializedName("avatar")
    val avatar: String = "",

    @SerializedName("gender")
    val gender: Int = 0,

    @SerializedName("region")
    val region: String = "",

    @SerializedName("member_level")
    val memberLevel: Int = 0,

    @SerializedName("member_expire_time")
    val memberExpireTime: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) : Parcelable {
    val isMember: Boolean
        get() = memberLevel > 0 && getMemberExpireTimeMillis() > System.currentTimeMillis()

    private fun parseDate(dateString: String?): Long {
        if (dateString == null) return 0L
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun getMemberExpireTimeMillis(): Long {
        return parseDate(memberExpireTime)
    }

    fun getCreatedAtMillis(): Long = parseDate(createdAt)
    fun getUpdatedAtMillis(): Long = parseDate(updatedAt)
}
