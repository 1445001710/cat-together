package com.cat_together.meta.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cat_together.meta.model.DietRecord

@Dao
interface DietRecordDao {

    @Query("SELECT * FROM diet_records WHERE catId = :catId ORDER BY timestamp DESC")
    fun getDietRecordsByCatId(catId: String): LiveData<List<DietRecord>>

    @Query("SELECT * FROM diet_records WHERE catId = :catId ORDER BY timestamp DESC")
    suspend fun getDietRecordsByCatIdList(catId: String): List<DietRecord>

    @Query("SELECT * FROM diet_records WHERE catId = :catId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getDietRecordsByCatIdAndTimeRange(
        catId: String,
        startTime: Long,
        endTime: Long
    ): List<DietRecord>

    @Query("SELECT * FROM diet_records WHERE catId = :catId AND type = :type ORDER BY timestamp DESC LIMIT 20")
    fun getDietRecordsByCatIdAndType(catId: String, type: Int): LiveData<List<DietRecord>>

    @Query("SELECT * FROM diet_records WHERE id = :recordId")
    suspend fun getDietRecordById(recordId: String): DietRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietRecord(record: DietRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietRecords(records: List<DietRecord>)

    @Update
    suspend fun updateDietRecord(record: DietRecord)

    @Delete
    suspend fun deleteDietRecord(record: DietRecord)

    @Query("DELETE FROM diet_records WHERE id = :recordId")
    suspend fun deleteDietRecordById(recordId: String)

    @Query("DELETE FROM diet_records WHERE catId = :catId")
    suspend fun deleteDietRecordsByCatId(catId: String)

    @Query("SELECT COUNT(*) FROM diet_records WHERE catId = :catId AND type = :type AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getDietRecordCount(
        catId: String,
        type: Int,
        startTime: Long,
        endTime: Long
    ): Int
}
