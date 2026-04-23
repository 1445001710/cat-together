package com.cat_together.meta.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int = 0,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: T? = null
) {
    val isSuccess: Boolean
        get() = code == 200 || code == 0
}

data class LoginResponse(
    @SerializedName("token")
    val token: String = "",

    @SerializedName("user")
    val user: User? = null
)

data class SendCodeResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)

data class PaginatedResponse<T>(
    @SerializedName("list")
    val list: List<T> = emptyList(),

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("pageSize")
    val pageSize: Int = 20
)
