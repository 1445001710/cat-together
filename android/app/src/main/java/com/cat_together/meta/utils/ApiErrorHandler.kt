package com.cat_together.meta.utils

import android.content.Context
import android.widget.Toast
import com.cat_together.meta.network.RateLimitException
import com.google.gson.Gson

/**
 * 全局 API 错误处理工具
 */
object ApiErrorHandler {

    private val gson = Gson()

    /**
     * 处理 API 异常，根据异常类型显示对应的错误提示
     * @param context Context
     * @param e 异常
     * @param defaultMessage 默认错误提示
     */
    fun handle(context: Context, e: Exception, defaultMessage: String = "请求失败") {
        when (e) {
            is RateLimitException -> {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            is java.net.UnknownHostException -> {
                Toast.makeText(context, "登录状态失效，请重新登录", Toast.LENGTH_SHORT).show()
            }
            is java.net.SocketTimeoutException -> {
                Toast.makeText(context, "登录状态失效，请重新登录", Toast.LENGTH_SHORT).show()
            }
            is java.io.IOException -> {
                Toast.makeText(context, "登录状态失效，请重新登录", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // 解析后端返回的错误信息
                val message = parseServerError(e, defaultMessage)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 处理 API 异常，返回错误消息字符串
     */
    fun getMessage(e: Exception, defaultMessage: String = "请求失败"): String {
        return when (e) {
            is RateLimitException -> e.message ?: "请求过于频繁"
            is java.net.UnknownHostException -> "登录状态失效，请重新登录"
            is java.net.SocketTimeoutException -> "登录状态失效，请重新登录"
            is java.io.IOException -> "登录状态失效，请重新登录"
            else -> parseServerError(e, defaultMessage)
        }
    }

    /**
     * 解析服务端返回的错误信息
     */
    private fun parseServerError(e: Exception, defaultMessage: String): String {
        val message = e.message ?: return defaultMessage

        // 尝试解析 JSON 格式的错误信息
        return try {
            if (message.contains("{") && message.contains("}")) {
                val jsonMap = gson.fromJson(message, Map::class.java)
                jsonMap["message"]?.toString() ?: defaultMessage
            } else {
                defaultMessage
            }
        } catch (e: Exception) {
            defaultMessage
        }
    }

    /**
     * 从 errorBody 解析错误信息（Retrofit 响应错误时调用）
     */
    fun parseErrorBody(errorBody: String?, defaultMessage: String = "请求失败"): String {
        return try {
            if (errorBody.isNullOrEmpty()) return defaultMessage
            val jsonMap = gson.fromJson(errorBody, Map::class.java)
            jsonMap["message"]?.toString() ?: defaultMessage
        } catch (e: Exception) {
            defaultMessage
        }
    }
}