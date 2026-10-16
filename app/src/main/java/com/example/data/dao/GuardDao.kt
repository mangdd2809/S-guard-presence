package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GuardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardDao {
    @Query("SELECT * FROM guards ORDER BY role ASC, name ASC")
    fun getAllGuards(): Flow<List<GuardEntity>>

    @Query("SELECT * FROM guards WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveGuards(): Flow<List<GuardEntity>>

    @Query("SELECT * FROM guards WHERE id = :id LIMIT 1")
    suspend fun getGuardById(id: String): GuardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuard(guard: GuardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuards(guards: List<GuardEntity>)

    @Update
    suspend fun updateGuard(guard: GuardEntity)

    @Delete
    suspend fun deleteGuard(guard: GuardEntity)

    @Query("SELECT COUNT(*) FROM guards")
    suspend fun getCount(): Int
}
