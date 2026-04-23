package com.cat_together.meta.utils

import android.content.Context
import android.content.SharedPreferences
import com.cat_together.meta.model.User
import com.google.gson.Gson

class SharedPreferencesHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "CatTogetherPrefs",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
        private const val KEY_USERNAME = "username"

        @Volatile
        private var instance: SharedPreferencesHelper? = null

        fun getInstance(context: Context): SharedPreferencesHelper {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    var token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var user: User?
        get() {
            return try {
                val json = prefs.getString(KEY_USER, null)
                if (json != null) {
                    android.util.Log.d("SharedPreferencesHelper", "User JSON: $json")
                    val user = gson.fromJson(json, User::class.java)
                    android.util.Log.d("SharedPreferencesHelper", "Parsed user: $user")
                    user
                } else {
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("SharedPreferencesHelper", "Error parsing user", e)
                null
            }
        }
        set(value) {
            try {
                val json = if (value != null) {
                    gson.toJson(value).also {
                        android.util.Log.d("SharedPreferencesHelper", "Saving user JSON: $it")
                    }
                } else null
                prefs.edit().putString(KEY_USER, json).apply()
            } catch (e: Exception) {
                android.util.Log.e("SharedPreferencesHelper", "Error saving user", e)
            }
        }

    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    fun isLoggedIn(): Boolean = token.isNotEmpty() && user != null

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun clearUserData() {
        token = ""
        user = null
    }


    fun saveToken(token: String) {
        this.token = token
    }

    fun saveUser(user: User) {
        this.user = user
    }

    fun saveUsername(username: String) {
        this.username = username
    }

    fun getUserId(): String {
        return user?.id ?: ""
    }

    fun clearUserInfo() {
        token = ""
        user = null
        username = ""
    }
}
