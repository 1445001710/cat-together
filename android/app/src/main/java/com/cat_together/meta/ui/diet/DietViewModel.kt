package com.cat_together.meta.ui.diet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cat_together.meta.CatTogetherApp
import com.cat_together.meta.model.DietRecord
import com.cat_together.meta.model.FeedingReminder
import com.cat_together.meta.network.RetrofitClient
import com.cat_together.meta.utils.ApiErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DietViewModel : ViewModel() {

    private val _records = MutableLiveData<List<DietRecord>>()
    val records: LiveData<List<DietRecord>> = _records

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun clearError() {
        _error.value = ""
    }

    private val _currentCat = MutableLiveData<String>()
    val currentCat: LiveData<String> = _currentCat

    private val _reminders = MutableLiveData<List<FeedingReminder>>()
    val reminders: LiveData<List<FeedingReminder>> = _reminders

    /**
     * 设置当前选中的猫咪
     * @param catId 猫咪 ID
     * @param forceReload 是否强制从服务器刷新
     */
    fun setCurrentCat(catId: String, forceReload: Boolean = false) {
        _currentCat.value = catId
        loadDietRecords(catId)
        if (forceReload) {
            loadReminders(catId)
        } else {
            // 只从本地数据库加载，不从服务器刷新（避免覆盖本地未同步的更改）
            loadRemindersFromLocal(catId)
        }
    }

    /**
     * 从本地数据库加载提醒（不触发服务器请求）
     */
    fun loadRemindersFromLocal(catId: String) {
        viewModelScope.launch {
            val localReminders = withContext(Dispatchers.IO) {
                CatTogetherApp.database.feedingReminderDao().getRemindersByCatIdList(catId)
            }
            _reminders.value = localReminders
        }
    }

    /**
     * 加载饮食记录列表
     */
    fun loadDietRecords(catId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 先从后端加载
                val response = RetrofitClient.apiService.getDietRecords(catId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val records = response.body()?.data ?: emptyList()

                    // 先更新UI，再保存到本地数据库
                    _records.value = records

                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.dietRecordDao().deleteDietRecordsByCatId(catId)
                        records.forEach { record ->
                            CatTogetherApp.database.dietRecordDao().insertDietRecord(record)
                        }
                    }
                } else {
                    // 后端加载失败，从本地数据库加载
                    val localRecords = withContext(Dispatchers.IO) {
                        CatTogetherApp.database.dietRecordDao().getDietRecordsByCatIdList(catId)
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
                        CatTogetherApp.database.dietRecordDao().getDietRecordsByCatIdList(catId)
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
     * 加载单条饮食记录
     */
    fun loadDietRecord(recordId: String, callback: (DietRecord?) -> Unit) {
        viewModelScope.launch {
            try {
                val record = withContext(Dispatchers.IO) {
                    CatTogetherApp.database.dietRecordDao().getDietRecordById(recordId)
                }
                callback(record)
            } catch (e: Exception) {
                _error.value = ApiErrorHandler.getMessage(e, "加载失败")
                callback(null)
            }
        }
    }

    /**
     * 加载喂食提醒列表
     */
    fun loadReminders(catId: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 直接从服务器加载（确保数据一致性）
                val response = RetrofitClient.apiService.getReminders()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    var reminders = response.body()?.data ?: emptyList()

                    // 如果指定了catId，过滤提醒
                    if (catId != null) {
                        reminders = reminders.filter { it.catId == catId }
                    }

                    // 更新UI
                    _reminders.value = reminders

                    // 保存到本地数据库（使用服务器返回的 ID）
                    withContext(Dispatchers.IO) {
                        if (catId != null) {
                            CatTogetherApp.database.feedingReminderDao().deleteRemindersByCatId(catId)
                        } else {
                            CatTogetherApp.database.feedingReminderDao().deleteAllReminders()
                        }
                        reminders.forEach { reminder ->
                            CatTogetherApp.database.feedingReminderDao().insertReminder(reminder)
                        }
                    }
                } else {
                    // 服务器加载失败，从本地数据库加载
                    val localReminders = withContext(Dispatchers.IO) {
                        if (catId != null) {
                            CatTogetherApp.database.feedingReminderDao().getRemindersByCatIdList(catId)
                        } else {
                            CatTogetherApp.database.feedingReminderDao().getAllRemindersList()
                        }
                    }
                    _reminders.value = localReminders
                    _error.value = ApiErrorHandler.getMessage(Exception(response.body()?.message ?: "从网络加载失败，已使用本地数据"), "从网络加载失败，已使用本地数据")
                }

                _isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，从本地数据库加载
                try {
                    val localReminders = withContext(Dispatchers.IO) {
                        if (catId != null) {
                            CatTogetherApp.database.feedingReminderDao().getRemindersByCatIdList(catId)
                        } else {
                            CatTogetherApp.database.feedingReminderDao().getAllRemindersList()
                        }
                    }
                    _reminders.value = localReminders
                    _error.value = "登录状态失效，已使用本地数据"
                } catch (dbException: Exception) {
                    _error.value = ApiErrorHandler.getMessage(e, "加载失败")
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载单条提醒
     */
    fun loadReminder(reminderId: String, callback: (FeedingReminder?) -> Unit) {
        viewModelScope.launch {
            try {
                val reminder = withContext(Dispatchers.IO) {
                    CatTogetherApp.database.feedingReminderDao().getReminderById(reminderId)
                }
                callback(reminder)
            } catch (e: Exception) {
                _error.value = ApiErrorHandler.getMessage(e, "加载失败")
                callback(null)
            }
        }
    }

    /**
     * 保存饮食记录（新增或更新）
     */
    fun saveDietRecord(record: DietRecord, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (record.id.isNotEmpty()) {
                    // 更新 - 暂不支持后端更新
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.dietRecordDao().updateDietRecord(record)
                    }
                    loadDietRecords(record.catId)
                    callback(true)
                } else {
                    // 新增 - 先调用后端
                    val response = RetrofitClient.apiService.addDietRecord(
                        com.cat_together.meta.network.AddDietRecordRequest(
                            catId = record.catId,
                            type = record.type,
                            amount = record.amount,
                            note = record.note
                        )
                    )

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val newRecord = response.body()?.data
                        if (newRecord != null) {
                            withContext(Dispatchers.IO) {
                                CatTogetherApp.database.dietRecordDao().insertDietRecord(newRecord)
                            }
                        }
                        loadDietRecords(record.catId)
                        callback(true)
                    } else {
                        // 后端失败，保存到本地
                        val localRecord = record.copy(id = generateId())
                        withContext(Dispatchers.IO) {
                            CatTogetherApp.database.dietRecordDao().insertDietRecord(localRecord)
                        }
                        loadDietRecords(record.catId)
                        callback(true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，保存到本地
                try {
                    val localRecord = if (record.id.isEmpty()) record.copy(id = generateId()) else record
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.dietRecordDao().insertDietRecord(localRecord)
                    }
                    loadDietRecords(record.catId)
                    callback(true)
                } catch (dbException: Exception) {
                    _error.value = ApiErrorHandler.getMessage(e, "保存失败")
                    callback(false)
                }
            }
        }
    }

    /**
     * 保存喂食提醒（新增或更新）
     */
    fun saveReminder(reminder: FeedingReminder, callback: (Boolean, FeedingReminder?) -> Unit) {
        viewModelScope.launch {
            try {
                if (reminder.id.isNotEmpty()) {
                    // 更新 - 调用后端API
                    val response = RetrofitClient.apiService.updateReminder(
                        reminder.id,
                        com.cat_together.meta.network.UpdateReminderRequest(
                            type = reminder.type,
                            time = reminder.time,
                            repeatRule = reminder.repeatRule,
                            enabled = reminder.enabled
                        )
                    )

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        // 直接更新 LiveData，而不是重新加载（避免服务器数据覆盖本地修改）
                        val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                        val index = currentList.indexOfFirst { it.id == reminder.id }
                        if (index >= 0) {
                            currentList[index] = reminder
                            _reminders.value = currentList.toList()
                        }
                        withContext(Dispatchers.IO) {
                            CatTogetherApp.database.feedingReminderDao().updateReminder(reminder)
                        }
                        // 刷新饮食记录
                        loadDietRecords(reminder.catId)
                        callback(true, null)
                    } else {
                        // 后端失败，仍更新本地
                        val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                        val index = currentList.indexOfFirst { it.id == reminder.id }
                        if (index >= 0) {
                            currentList[index] = reminder
                            _reminders.value = currentList.toList()
                        }
                        withContext(Dispatchers.IO) {
                            CatTogetherApp.database.feedingReminderDao().updateReminder(reminder)
                        }
                        loadDietRecords(reminder.catId)
                        callback(true, null)
                    }
                } else {
                    // 新增 - 调用后端API
                    val response = RetrofitClient.apiService.createReminder(
                        com.cat_together.meta.network.CreateReminderRequest(
                            catId = reminder.catId,
                            type = reminder.type,
                            time = reminder.time,
                            repeatRule = reminder.repeatRule
                        )
                    )

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val newReminder = response.body()?.data
                        if (newReminder != null) {
                            withContext(Dispatchers.IO) {
                                CatTogetherApp.database.feedingReminderDao().insertReminder(newReminder)
                            }
                            // 直接更新 LiveData，而不是重新加载（避免服务器数据覆盖本地修改）
                            val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                            currentList.add(newReminder)
                            _reminders.value = currentList.toList()
                        }
                        // 刷新饮食记录
                        loadDietRecords(reminder.catId)
                        // 返回服务器创建的提醒（包含正确的ID）
                        callback(true, newReminder)
                    } else {
                        // 后端失败，保存到本地
                        val localReminder = reminder.copy(id = generateReminderId())
                        withContext(Dispatchers.IO) {
                            CatTogetherApp.database.feedingReminderDao().insertReminder(localReminder)
                        }
                        // 直接更新 LiveData，而不是重新加载
                        val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                        currentList.add(localReminder)
                        _reminders.value = currentList.toList()
                        loadDietRecords(reminder.catId)
                        callback(true, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，保存到本地
                try {
                    val localReminder = if (reminder.id.isEmpty()) reminder.copy(id = generateReminderId()) else reminder
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.feedingReminderDao().insertReminder(localReminder)
                    }
                    // 直接更新 LiveData，而不是重新加载
                    val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                    currentList.add(localReminder)
                    _reminders.value = currentList.toList()
                    loadDietRecords(reminder.catId)
                    callback(true, null)
                } catch (dbException: Exception) {
                    _error.value = ApiErrorHandler.getMessage(e, "保存失败")
                    callback(false, null)
                }
            }
        }
    }

    /**
     * 删除提醒
     */
    fun deleteReminder(reminder: FeedingReminder, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 先调用后端删除
                val response = RetrofitClient.apiService.deleteReminder(reminder.id)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.feedingReminderDao().deleteReminderById(reminder.id)
                    }
                    // 直接更新 LiveData，而不是重新加载
                    val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                    currentList.removeAll { it.id == reminder.id }
                    _reminders.value = currentList.toList()
                } else {
                    // 后端失败，仍删除本地
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.feedingReminderDao().deleteReminderById(reminder.id)
                    }
                    // 直接更新 LiveData，而不是重新加载
                    val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                    currentList.removeAll { it.id == reminder.id }
                    _reminders.value = currentList.toList()
                }
                callback(true)
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，仍删除本地
                try {
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.feedingReminderDao().deleteReminderById(reminder.id)
                    }
                    // 直接更新 LiveData，而不是重新加载
                    val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                    currentList.removeAll { it.id == reminder.id }
                    _reminders.value = currentList.toList()
                    callback(true)
                } catch (dbException: Exception) {
                    _error.value = ApiErrorHandler.getMessage(e, "删除失败")
                    callback(false)
                }
            }
        }
    }

    /**
     * 更新提醒启用状态
     */
    fun updateReminderEnabled(reminder: FeedingReminder, enabled: Boolean) {
        viewModelScope.launch {
            try {
                // 从当前 LiveData 中获取最新的 reminder（确保 ID 是正确的）
                val currentReminder = _reminders.value?.find { it.id == reminder.id } ?: reminder
                val updatedReminder = currentReminder.copy(enabled = enabled)

                // 先直接更新本地数据库和 LiveData（乐观更新，立即响应用户操作）
                withContext(Dispatchers.IO) {
                    CatTogetherApp.database.feedingReminderDao().updateReminder(updatedReminder)
                }
                // 直接更新 LiveData 中的数据，而不是重新加载
                val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
                val index = currentList.indexOfFirst { it.id == currentReminder.id }
                if (index >= 0) {
                    currentList[index] = updatedReminder
                    _reminders.value = currentList.toList()
                }

                // 异步调用后端更新（不阻塞 UI）
                try {
                    val response = RetrofitClient.apiService.updateReminder(
                        currentReminder.id,
                        com.cat_together.meta.network.UpdateReminderRequest(
                            type = updatedReminder.type,
                            time = updatedReminder.time,
                            repeatRule = updatedReminder.repeatRule,
                            enabled = enabled
                        )
                    )
                    // 即使后端返回错误，本地已经更新，下次同步时会自动纠正
                } catch (apiException: Exception) {
                    // API 调用失败，但本地已经更新，用户可以看到变化
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = ApiErrorHandler.getMessage(e, "更新失败")
            }
        }
    }

    /**
     * 删除饮食记录
     */
    fun deleteDietRecord(record: DietRecord, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 先调用后端删除
                val response = RetrofitClient.apiService.deleteDietRecord(record.id)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.dietRecordDao().deleteDietRecord(record)
                    }
                } else {
                    // 后端失败，仍删除本地
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.dietRecordDao().deleteDietRecord(record)
                    }
                }
                loadDietRecords(record.catId)
                callback(true)
            } catch (e: Exception) {
                e.printStackTrace()
                // 网络错误，仍删除本地
                try {
                    withContext(Dispatchers.IO) {
                        CatTogetherApp.database.dietRecordDao().deleteDietRecord(record)
                    }
                    loadDietRecords(record.catId)
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
        return "diet_${System.currentTimeMillis()}"
    }

    /**
     * 生成提醒唯一ID
     */
    private fun generateReminderId(): String {
        return "reminder_${System.currentTimeMillis()}"
    }
}