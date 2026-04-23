package com.cat_together.meta.ui.health

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cat_together.meta.CatTogetherApp
import com.cat_together.meta.model.HealthRecord
import com.cat_together.meta.network.RetrofitClient
import com.cat_together.meta.utils.ApiErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HealthViewModel : ViewModel() {

    private val _records = MutableLiveData<List<HealthRecord>>()
    val records: LiveData<List<HealthRecord>> = _records

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun clearError() {
        _error.value = ""
    }

    private val _currentCat = MutableLiveData<String>()
    val currentCat: LiveData<String> = _currentCat

    /**
     * 设置当前选中的猫咪
     */
    fun setCurrentCat(catId: String) {
        _currentCat.value = catId
        loadHealthRecords(catId)
    }

    /**
     * 加载健康记录列表
     */
    fun loadHealthRecords(catId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 先从后端加载
                val response = RetrofitClient.apiService.getHealthRecords(catId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val paginatedResponse = response.body()?.data
                    val records = paginatedResponse?.list ?: emptyList()

                    // 保存到本地数据库
                    withContext(Dispatchers.IO) {
                        // 清空该猫咪的旧记录
                        CatTogetherApp.database.healthRecordDao().deleteHealthRecordsByCatId(catId)
                        // 插入新记录
                        records.forEach { record ->
                            CatTogetherApp.database.healthRecordDao().insertHealthRecord(record)
                        }
                    }

                    _records.value = records
                } else {
                    // 后端加载失败，从本地数据库加载
                    val localRecords = withContext(Dispatchers.IO) {
                        CatTogetherApp.database.healthRecordDao().getHealthRecordsByCatIdList(catId)
                    }
                    _records.value = localRecords
                    _error.value = ApiErrorHandler.getMessage(Exception(response.body()?.message ?: "从网络加载失败，已使用本地数据"), "从网络加载失败，已使用本地数据")
                }

                _isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，从本地数据库加载
                try {
                    val localRecords = withContext(Dispatchers.IO) {
                        CatTogetherApp.database.healthRecordDao().getHealthRecordsByCatIdList(catId)
                    }
                    _records.value = localRecords
                    _error.value = "登录状态失效，已使用本地数据"
                } catch (dbException: Exception) {
                    _error.value = ApiErrorHandler.getMessage(e, "加载失败")
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载单条健康记录
     */
    fun loadHealthRecord(recordId: String, callback: (HealthRecord?) -> Unit) {
        viewModelScope.launch {
            try {
                val record = withContext(Dispatchers.IO) {
                    CatTogetherApp.database.healthRecordDao().getHealthRecordById(recordId)
                }
                callback(record)
            } catch (e: Exception) {
                _error.value = ApiErrorHandler.getMessage(e, "加载失败")
                callback(null)
            }
        }
    }

    /**
     * 保存健康记录（新增或更新）
     */
    fun saveHealthRecord(record: HealthRecord, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (record.id.isNotEmpty()) {
                    // 更新 - 调用后端API
                    val response = RetrofitClient.apiService.updateHealthRecord(
                        record.catId,
                        record.id,
                        com.cat_together.meta.network.UpdateHealthRecordRequest(
                            recordType = record.recordType,
                            value = record.value,
                            recordDate = record.recordDate,
                            note = record.note
                        )
                    )

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val updatedRecord = response.body()?.data
                        if (updatedRecord != null) {
                            withContext(Dispatchers.IO) {
                                CatTogetherApp.database.healthRecordDao().updateHealthRecord(updatedRecord)
                            }
                        }
                        loadHealthRecords(record.catId)
                        callback(true)
                    } else {
                        // 后端失败，尝试本地更新
                        withContext(Dispatchers.IO) {
                            CatTogetherApp.database.healthRecordDao().updateHealthRecord(record)
                        }
                        loadHealthRecords(record.catId)
                        callback(true)
                    }
                } else {
                    // 新增 - 先调用后端
                    val response = RetrofitClient.apiService.addHealthRecord(
                        record.catId,
                        com.cat_together.meta.network.AddHealthRecordRequest(
                            recordType = record.recordType,
                            value = record.value,
                            recordDate = record.recordDate,
                            note = record.note
                        )
                    )

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val newRecord = response.body()?.data
                        if (newRecord != null) {
                            // 保存到本地数据库
                            withContext(Dispatchers.IO) {
                                CatTogetherApp.database.healthRecordDao().insertHealthRecord(newRecord)
                            }
                            // 刷新列表
                            loadHealthRecords(record.catId)
                        }
                        callback(true)
                    } else {
                        _error.value = ApiErrorHandler.getMessage(Exception(response.body()?.message ?: "保存失败"), "保存失败")
                        callback(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = ApiErrorHandler.getMessage(e, "保存失败")
                callback(false)
            }
        }
    }

    /**
     * 删除健康记录
     */
    fun deleteHealthRecord(record: HealthRecord, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 先调用后端删除
                val response = RetrofitClient.apiService.deleteHealthRecord(record.catId, record.id)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 后端删除成功，删除本地
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.healthRecordDao().deleteHealthRecord(record)
                    }
                } else {
                    // 后端删除失败，仍删除本地（离线场景）
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.healthRecordDao().deleteHealthRecord(record)
                    }
                }
                // 刷新列表
                record.catId.takeIf { it.isNotEmpty() }?.let {
                    loadHealthRecords(it)
                }
                callback(true)
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，仍删除本地
                try {
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.healthRecordDao().deleteHealthRecord(record)
                    }
                    record.catId.takeIf { it.isNotEmpty() }?.let {
                        loadHealthRecords(it)
                    }
                    callback(true)
                } catch (dbException: Exception) {
                    _error.value = ApiErrorHandler.getMessage(e, "删除失败")
                    callback(false)
                }
            }
        }
    }

    /**
     * 生成唯一ID
     */
    private fun generateId(): String {
        return "health_${System.currentTimeMillis()}"
    }
}