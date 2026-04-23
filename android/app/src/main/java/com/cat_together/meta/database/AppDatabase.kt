package com.cat_together.meta.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.cat_together.meta.database.dao.CatDao
import com.cat_together.meta.database.dao.DietRecordDao
import com.cat_together.meta.database.dao.FeedingReminderDao
import com.cat_together.meta.database.dao.HealthRecordDao
import com.cat_together.meta.database.dao.ReminderDao
import com.cat_together.meta.model.Cat
import com.cat_together.meta.model.DietRecord
import com.cat_together.meta.model.FeedingReminder
import com.cat_together.meta.model.HealthRecord

@Database(
    entities = [Cat::class, HealthRecord::class, DietRecord::class, FeedingReminder::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun catDao(): CatDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun dietRecordDao(): DietRecordDao
    abstract fun reminderDao(): ReminderDao
    abstract fun feedingReminderDao(): FeedingReminderDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cat_together_db"
                ).fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
