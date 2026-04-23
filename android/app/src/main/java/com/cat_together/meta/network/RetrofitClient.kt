package com.cat_together.meta.network

import android.content.Context
import android.util.Log
import com.cat_together.meta.BuildConfig
import com.cat_together.meta.utils.SharedPreferencesHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

// 自定义限流异常
class RateLimitException(val retryAfter: Int, message: String) : IOException(message)

object RetrofitClient {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (com.cat_together.meta.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val context = appContext
        val token = if (context != null) {
            SharedPreferencesHelper.getInstance(context).token
        } else {
            ""
        }

        val request = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(request)
    }

    // 签名密钥（需要与服务端 SIGN_SECRET 一致）
    private const val SIGN_SECRET = "8fe819e196555f73b8c29fbd530ea1191bbf16a297da9aa7037a1f75cf7d64d4"

    private fun generateNonce(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // 格式化 JSON 值为字符串，避免科学计数法
    private fun formatValue(value: Any?): String {
        if (value == null) return ""
        return when (value) {
            is Double -> {
                if (value == value.toLong().toDouble()) {
                    value.toLong().toString()
                } else {
                    value.toString()
                }
            }
            is Float -> value.toString()
            is Number -> value.toString()
            else -> value.toString()
        }
    }

    // 签名拦截器
    private val signInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // 如果没有 token，跳过签名（登录前的请求）
        val context = appContext
        val token = if (context != null) {
            SharedPreferencesHelper.getInstance(context).token
        } else {
            ""
        }
        if (token.isEmpty()) {
            return@Interceptor chain.proceed(originalRequest)
        }

        // 生成时间戳和 Nonce
        val timestamp = System.currentTimeMillis().toString()
        val nonce = generateNonce()

        // 获取当前请求的参数
        val requestParams = mutableMapOf<String, String>()

        // 添加 URL query 参数
        originalRequest.url.queryParameterNames.forEach { name ->
            originalRequest.url.queryParameter(name)?.let { value ->
                requestParams[name] = value
            }
        }

        // 添加请求体参数（JSON body）
        val requestBody = originalRequest.body
        if (requestBody != null) {
            try {
                val buffer = okio.Buffer()
                requestBody.writeTo(buffer)
                val bodyString = buffer.readUtf8()
                val jsonMap = gson.fromJson(bodyString, Map::class.java)
                jsonMap?.forEach { (key, value) ->
                    requestParams[key.toString()] = formatValue(value)
                }
            } catch (e: Exception) {
                Log.e("SignInterceptor", "Failed to parse body", e)
            }
        }

        // 构建待签名的字符串
        val sortedParams = requestParams.entries
            .filter { it.value.isNotEmpty() }
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" }
        val signString = "${timestamp}&${sortedParams}&${SIGN_SECRET}"

        Log.d("SignInterceptor", "Sign string: $signString")

        // SHA256 签名
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(signString.toByteArray(Charsets.UTF_8))
        val sign = hashBytes.joinToString("") { "%02x".format(it) }

        // 添加签名相关的 header
        val signedRequest = originalRequest.newBuilder()
            .header("X-Timestamp", timestamp)
            .header("X-Nonce", nonce)
            .header("X-Sign", sign)
            .build()

        chain.proceed(signedRequest)
    }

    // 错误处理拦截器（处理 429 限流等错误）
    private val errorInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response: Response? = null
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            throw e
        }

        // 检查 429 限流响应
        if (response.code == 429) {
            val retryAfter = response.header("Retry-After")?.toIntOrNull() ?: 900 // 默认15分钟
            response.close()

            // 尝试解析错误信息
            val errorMessage = try {
                response.body?.string()?.let { body ->
                    val jsonMap = gson.fromJson(body, Map::class.java)
                    jsonMap["message"]?.toString() ?: "请求过于频繁"
                }
            } catch (e: Exception) {
                "请求过于频繁"
            }

            val minutes = retryAfter / 60
            val displayMessage = if (minutes > 0) {
                "请求过于频繁，请在${minutes}分钟后重试"
            } else {
                "请求过于频繁，请在${retryAfter}秒后重试"
            }

            throw RateLimitException(retryAfter, displayMessage)
        }

        return@Interceptor response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(signInterceptor)
        .addInterceptor(errorInterceptor) // 错误拦截器放在最后
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: RetrofitService by lazy {
        retrofit.create(RetrofitService::class.java)
    }
}