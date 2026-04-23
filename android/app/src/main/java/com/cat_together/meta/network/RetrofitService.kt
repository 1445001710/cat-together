package com.cat_together.meta.network

import com.cat_together.meta.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface RetrofitService {

    // ==================== 认证相关 ====================
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // ==================== 用户相关 ====================
    @GET("user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<User>>

    @PUT("user/profile")
    suspend fun updateUserProfile(@Body body: UpdateUserRequest): Response<ApiResponse<User>>

    @Multipart
    @POST("user/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<ApiResponse<User>>

    // ==================== 猫咪相关 ====================
    @POST("cats")
    suspend fun createCat(@Body body: CreateCatRequest): Response<ApiResponse<Cat>>

    @GET("cats")
    suspend fun getCats(): Response<ApiResponse<List<Cat>>>

    @GET("cats/{id}")
    suspend fun getCat(@Path("id") catId: String): Response<ApiResponse<Cat>>

    @PUT("cats/{id}")
    suspend fun updateCat(
        @Path("id") catId: String,
        @Body body: UpdateCatRequest
    ): Response<ApiResponse<Cat>>

    @DELETE("cats/{id}")
    suspend fun deleteCat(@Path("id") catId: String): Response<ApiResponse<Unit>>

    @POST("cats/{id}/health")
    suspend fun addHealthRecord(
        @Path("id") catId: String,
        @Body body: AddHealthRecordRequest
    ): Response<ApiResponse<HealthRecord>>

    @GET("cats/{id}/health")
    suspend fun getHealthRecords(
        @Path("id") catId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ApiResponse<PaginatedResponse<HealthRecord>>>

    @PUT("cats/{id}/health/{recordId}")
    suspend fun updateHealthRecord(
        @Path("id") catId: String,
        @Path("recordId") recordId: String,
        @Body body: UpdateHealthRecordRequest
    ): Response<ApiResponse<HealthRecord>>

    @DELETE("cats/{id}/health/{recordId}")
    suspend fun deleteHealthRecord(
        @Path("id") catId: String,
        @Path("recordId") recordId: String
    ): Response<ApiResponse<Unit>>

    @POST("cats/{id}/weight")
    suspend fun updateWeight(
        @Path("id") catId: String,
        @Body body: UpdateWeightRequest
    ): Response<ApiResponse<Unit>>

    // ==================== 饮食相关 ====================
    @POST("diet/record")
    suspend fun addDietRecord(@Body body: AddDietRecordRequest): Response<ApiResponse<DietRecord>>

    @DELETE("diet/record/{id}")
    suspend fun deleteDietRecord(@Path("id") recordId: String): Response<ApiResponse<Unit>>

    @GET("diet/records/{catId}")
    suspend fun getDietRecords(
        @Path("catId") catId: String,
        @Query("startDate") startDate: Long? = null,
        @Query("endDate") endDate: Long? = null
    ): Response<ApiResponse<List<DietRecord>>>

    @POST("diet/reminder")
    suspend fun createReminder(@Body body: CreateReminderRequest): Response<ApiResponse<FeedingReminder>>

    @GET("diet/reminders")
    suspend fun getReminders(): Response<ApiResponse<List<FeedingReminder>>>

    @PUT("diet/reminder/{id}")
    suspend fun updateReminder(
        @Path("id") reminderId: String,
        @Body body: UpdateReminderRequest
    ): Response<ApiResponse<FeedingReminder>>

    @DELETE("diet/reminder/{id}")
    suspend fun deleteReminder(@Path("id") reminderId: String): Response<ApiResponse<Unit>>

    @GET("diet/stats/{catId}")
    suspend fun getDietStats(@Path("catId") catId: String): Response<ApiResponse<DietStats>>

    // ==================== 相册相关 ====================
    @Multipart
    @POST("media/upload")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Part("catId") catId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("tags") tags: RequestBody?
    ): Response<ApiResponse<Media>>

    @GET("media/list")
    suspend fun getMediaList(
        @Query("catId") catId: String? = null,
        @Query("type") type: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ApiResponse<PaginatedResponse<Media>>>

    @DELETE("media/{id}")
    suspend fun deleteMedia(@Path("id") mediaId: String): Response<ApiResponse<Unit>>

    // ==================== 社交相关 ====================
    @POST("posts")
    suspend fun createPost(@Body body: CreatePostRequest): Response<ApiResponse<Post>>

    @GET("posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ApiResponse<PaginatedResponse<Post>>>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") postId: String): Response<ApiResponse<Post>>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): Response<ApiResponse<Unit>>

    @POST("posts/{id}/like")
    suspend fun likePost(@Path("id") postId: String): Response<ApiResponse<Unit>>

    @DELETE("posts/{id}/like")
    suspend fun unlikePost(@Path("id") postId: String): Response<ApiResponse<Unit>>

    @POST("posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: String,
        @Body body: AddCommentRequest
    ): Response<ApiResponse<Comment>>

    @GET("posts/{id}/comments")
    suspend fun getComments(
        @Path("id") postId: String,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<PaginatedResponse<Comment>>>

    // ==================== AI相关 ====================
    @Multipart
    @POST("ai/upload-audio")
    suspend fun uploadAudio(@Part file: MultipartBody.Part): Response<ApiResponse<String>>

    @POST("ai/cat-speak")
    suspend fun catSpeak(@Body body: CatSpeakRequest): Response<ApiResponse<CatSpeakResponse>>

    @POST("ai/chat")
    suspend fun chat(@Body body: ChatRequest): Response<ApiResponse<ChatResponse>>
}

// ==================== 请求模型 ====================
data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String, val nickname: String)
data class UpdateUserRequest(val nickname: String, val avatar: String)

data class CreateCatRequest(
    val name: String,
    val breed: String,
    val gender: Int,
    val birthday: Long,
    val color: String,
    val weight: Float,
    val height: Float,
    val avatar: String? = null
)

data class UpdateCatRequest(
    val name: String,
    val breed: String,
    val color: String,
    val weight: Float,
    val height: Float,
    val avatar: String? = null
)

data class AddHealthRecordRequest(
    val recordType: Int,
    val value: Float,
    val recordDate: Long,
    val note: String
)

data class UpdateHealthRecordRequest(
    val recordType: Int,
    val value: Float,
    val recordDate: Long,
    val note: String
)

data class UpdateWeightRequest(val weight: Float)

data class AddDietRecordRequest(
    val catId: String,
    val type: Int,
    val amount: Int,
    val note: String
)

data class CreateReminderRequest(
    val catId: String,
    val type: Int,
    val time: String,
    val repeatRule: String
)

data class UpdateReminderRequest(
    val type: Int,
    val time: String,
    val repeatRule: String,
    val enabled: Boolean
)

data class CreatePostRequest(
    val content: String,
    val catId: String,
    val mediaIds: List<String>,
    val hashtags: List<String>
)

data class AddCommentRequest(val content: String, val parentId: String? = null)

data class CatSpeakRequest(val audioUrl: String)
data class CatSpeakResponse(
    val translation: String,
    val emotion: String,
    val need: String
)

data class ChatRequest(val message: String, val history: List<ChatMessage>? = null)
data class ChatResponse(val response: String)

data class ChatMessage(val role: String, val content: String)

data class Comment(
    val id: String,
    val userId: String,
    val postId: String,
    val parentId: String?,
    val content: String,
    val createdAt: Long,
    val user: User?
)

data class DietStats(
    val waterCount: Int,
    val foodCount: Int,
    val snackCount: Int,
    val treatCount: Int
)
