package com.cat_together.meta.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cat_together.meta.model.Cat

@Dao
interface CatDao {

    @Query("SELECT * FROM cats")
    fun getAllCats(): LiveData<List<Cat>>

    @Query("SELECT * FROM cats")
    suspend fun getAllCatsList(): List<Cat>

    @Query("SELECT * FROM cats WHERE id = :catId")
    suspend fun getCatById(catId: String): Cat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCat(cat: Cat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCats(cats: List<Cat>)

    @Update
    suspend fun updateCat(cat: Cat)

    @Delete
    suspend fun deleteCat(cat: Cat)

    @Query("DELETE FROM cats WHERE id = :catId")
    suspend fun deleteCatById(catId: String)

    @Query("DELETE FROM cats")
    suspend fun deleteAllCats()
}
