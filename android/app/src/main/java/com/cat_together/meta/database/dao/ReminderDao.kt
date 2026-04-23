package com.cat_together.meta.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cat_together.meta.model.FeedingReminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM feeding_reminders")
    fun getAllReminders(): LiveData<List<FeedingReminder>>

    @Query("SELECT * FROM feeding_reminders WHERE catId = :catId")
    fun getRemindersByCatId(catId: String): LiveData<List<FeedingReminder>>

    @Query("SELECT * FROM feeding_reminders WHERE enabled = 1")
    fun getEnabledReminders(): LiveData<List<FeedingReminder>>

    @Query("SELECT * FROM feeding_reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: String): FeedingReminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: FeedingReminder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<FeedingReminder>)

    @Update
    suspend fun updateReminder(reminder: FeedingReminder)

    @Delete
    suspend fun deleteReminder(reminder: FeedingReminder)

    @Query("DELETE FROM feeding_reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: String)

    @Query("DELETE FROM feeding_reminders WHERE catId = :catId")
    suspend fun deleteRemindersByCatId(catId: String)

    @Query("DELETE FROM feeding_reminders")
    suspend fun deleteAllReminders()
}
