package com.cat_together.meta.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cat_together.meta.CatTogetherApp
import com.cat_together.meta.model.Cat
import com.cat_together.meta.network.RetrofitClient
import com.cat_together.meta.utils.ApiErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _cats = MutableLiveData<List<Cat>>()
    val cats: LiveData<List<Cat>> = _cats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun clearError() {
        _error.value = ""
    }

    private var hasShownMockData = false

    fun loadCats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 先尝试从后端加载
                val response = RetrofitClient.apiService.getCats()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val serverCats = response.body()?.data ?: emptyList()

                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.catDao().deleteAllCats()
                        CatTogetherApp.database.catDao().insertCats(serverCats)
                    }

                    _cats.value = serverCats
                } else {
                    // 后端加载失败，从本地数据库加载
                    val dbCats = withContext(Dispatchers.IO) {
                        CatTogetherApp.database.catDao().getAllCatsList()
                    }
                    _cats.value = dbCats
                    _error.value = ApiErrorHandler.getMessage(Exception(response.body()?.message ?: "从网络加载失败，已使用本地数据"), "从网络加载失败，已使用本地数据")
                }

                _isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，从本地数据库加载
                try {
                    val dbCats = withContext(Dispatchers.IO) {
                        CatTogetherApp.database.catDao().getAllCatsList()
                    }
                    _cats.value = dbCats
                    _error.value = "登录状态失效，已使用本地数据"
                } catch (dbException: Exception) {
                    _error.value = ApiErrorHandler.getMessage(e, "加载失败")
                }
                _isLoading.value = false
            }
        }
    }

    private fun getMockCats(): List<Cat> {
        return listOf(
            Cat(
                id = "1",
                userId = "mock_user",
                name = "小橘",
                breed = "橘猫",
                gender = Cat.GENDER_MALE,
                birthday = System.currentTimeMillis() - 2L * 365 * 24 * 60 * 60 * 1000,
                color = "橘色",
                avatar = "",
                weight = 4.5f,
                height = 25f
            ),
            Cat(
                id = "2",
                userId = "mock_user",
                name = "糯米",
                breed = "英短蓝猫",
                gender = Cat.GENDER_FEMALE,
                birthday = System.currentTimeMillis() - 1L * 365 * 24 * 60 * 60 * 1000,
                color = "蓝灰色",
                avatar = "",
                weight = 3.8f,
                height = 23f
            ),
            Cat(
                id = "3",
                userId = "mock_user",
                name = "汤圆",
                breed = "布偶猫",
                gender = Cat.GENDER_MALE,
                birthday = System.currentTimeMillis() - 6L * 30 * 24 * 60 * 60 * 1000,
                color = "白色",
                avatar = "",
                weight = 5.2f,
                height = 28f
            )
        )
    }

    fun deleteCat(cat: Cat) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    CatTogetherApp.database.catDao().deleteCat(cat)
                }
                loadCats()
            } catch (e: Exception) {
                _error.value = ApiErrorHandler.getMessage(e, "删除失败")
            }
        }
    }
}