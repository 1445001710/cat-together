package com.cat_together.meta.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cat_together.meta.model.HealthRecord

@Dao
interface HealthRecordDao {

    @Query("SELECT * FROM health_records WHERE catId = :catId ORDER BY recordDate DESC")
    fun getHealthRecordsByCatId(catId: String): LiveData<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE catId = :catId ORDER BY recordDate DESC")
    suspend fun getHealthRecordsByCatIdList(catId: String): List<HealthRecord>

    @Query("SELECT * FROM health_records WHERE catId = :catId AND recordType = :type ORDER BY recordDate DESC LIMIT 20")
    fun getHealthRecordsByCatIdAndType(catId: String, type: Int): LiveData<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE id = :recordId")
    suspend fun getHealthRecordById(recordId: String): HealthRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecords(records: List<HealthRecord>)

    @Update
    suspend fun updateHealthRecord(record: HealthRecord)

    @Delete
    suspend fun deleteHealthRecord(record: HealthRecord)

    @Query("DELETE FROM health_records WHERE id = :recordId")
    suspend fun deleteHealthRecordById(recordId: String)

    @Query("DELETE FROM health_records WHERE catId = :catId")
    suspend fun deleteHealthRecordsByCatId(catId: String)
}
