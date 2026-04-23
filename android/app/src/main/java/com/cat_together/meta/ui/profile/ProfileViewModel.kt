package com.cat_together.meta.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cat_together.meta.CatTogetherApp
import com.cat_together.meta.model.User
import com.cat_together.meta.utils.ApiErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {

    private val _userInfo = MutableLiveData<User?>()
    val userInfo: LiveData<User?> = _userInfo

    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> = _logoutResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun clearError() {
        _error.value = ""
    }

    /**
     * 加载用户信息
     */
    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileViewModel", "Loading user info...")
                val user = CatTogetherApp.sharedPreferencesHelper.user
                android.util.Log.d("ProfileViewModel", "User loaded: $user")
                _userInfo.value = user
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Error loading user info", e)
                _error.value = ApiErrorHandler.getMessage(e, "加载失败")
            }
        }
    }

    /**
     * 退出登录
     */
    fun logout() {
        viewModelScope.launch {
            try {
                CatTogetherApp.sharedPreferencesHelper.clearUserInfo()
                _logoutResult.value = true
            } catch (e: Exception) {
                _error.value = ApiErrorHandler.getMessage(e, "退出失败")
                _logoutResult.value = false
            }
        }
    }
}