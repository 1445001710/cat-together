package com.cat_together.meta.ui.cat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cat_together.meta.CatTogetherApp
import com.cat_together.meta.model.Cat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatProfileViewModel : ViewModel() {

    suspend fun getCatById(catId: String): Cat? {
        return withContext(Dispatchers.IO) {
            CatTogetherApp.database.catDao().getCatById(catId)
        }
    }

    suspend fun saveCat(cat: Cat, isNew: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            val isNewCat = isNew || cat.id.isEmpty()
            val catToSave = if (isNewCat) {
                cat.copy(id = java.util.UUID.randomUUID().toString())
            } else {
                cat
            }

            android.util.Log.d("CatProfileViewModel", "Saving cat: isNew=$isNewCat, catId=${catToSave.id}")

            // 如果是新增猫咪，调用后端API
            if (isNewCat) {
                try {
                    val response = com.cat_together.meta.network.RetrofitClient.apiService.createCat(
                        com.cat_together.meta.network.CreateCatRequest(
                            name = catToSave.name,
                            breed = catToSave.breed,
                            gender = catToSave.gender,
                            birthday = catToSave.birthday,
                            color = catToSave.color,
                            weight = catToSave.weight,
                            height = catToSave.height,
                            avatar = catToSave.avatar?.takeIf { it.isNotEmpty() }
                        )
                    )

                    android.util.Log.d("CatProfileViewModel", "Create cat response: ${response.code()}, ${response.message()}")

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val serverCat = response.body()?.data
                        if (serverCat != null) {
                            // 处理后端返回的null avatar
                            val catToInsert = if (serverCat.avatar == null) {
                                serverCat.copy(avatar = "")
                            } else {
                                serverCat
                            }
                            // 更新本地数据库中的猫咪信息
                            CatTogetherApp.database.catDao().insertCat(catToInsert)
                            android.util.Log.d("CatProfileViewModel", "Saved to database with server ID: ${catToInsert.id}")
                        } else {
                            // 后端成功但没有返回数据，使用本地数据
                            CatTogetherApp.database.catDao().insertCat(catToSave)
                            android.util.Log.d("CatProfileViewModel", "Saved to database with local ID: ${catToSave.id}")
                        }
                        true
                    } else {
                        val errorMsg = response.body()?.message ?: "后端保存失败"
                        android.util.Log.e("CatProfileViewModel", "Backend error: $errorMsg")
                        // 后端返回错误，不保存到本地
                        false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CatProfileViewModel", "Network error", e)
                    // 网络错误，不保存
                    false
                }
            } else {
                // 更新猫咪
                try {
                    val response = com.cat_together.meta.network.RetrofitClient.apiService.updateCat(
                        catToSave.id,
                        com.cat_together.meta.network.UpdateCatRequest(
                            name = catToSave.name,
                            breed = catToSave.breed,
                            color = catToSave.color,
                            weight = catToSave.weight,
                            height = catToSave.height,
                            avatar = catToSave.avatar?.takeIf { it.isNotEmpty() }
                        )
                    )

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val serverCat = response.body()?.data
                        if (serverCat != null) {
                            CatTogetherApp.database.catDao().insertCat(serverCat)
                        } else {
                            CatTogetherApp.database.catDao().insertCat(catToSave)
                        }
                        true
                    } else {
                        // 更新失败，不保存
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 网络错误，更新本地
                    CatTogetherApp.database.catDao().insertCat(catToSave)
                    true // 本地保存成功
                }
            }
        }
    }

    suspend fun deleteCatById(catId: String) {
        withContext(Dispatchers.IO) {
            // 调用后端删除
            try {
                com.cat_together.meta.network.RetrofitClient.apiService.deleteCat(catId)
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，继续删除本地数据
            }
            // 删除本地数据
            CatTogetherApp.database.catDao().deleteCatById(catId)
        }
    }
}