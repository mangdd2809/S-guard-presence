package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PermitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermitDao {
    @Query("SELECT * FROM permits ORDER BY createdAt DESC")
    fun getAllPermits(): Flow<List<PermitEntity>>

    @Query("SELECT * FROM permits WHERE guardId = :guardId ORDER BY createdAt DESC")
    fun getPermitsByGuard(guardId: String): Flow<List<PermitEntity>>

    @Query("SELECT * FROM permits WHERE status = :status ORDER BY createdAt DESC")
    fun getPermitsByStatus(status: String): Flow<List<PermitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermit(permit: PermitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermits(permits: List<PermitEntity>)

    @Update
    suspend fun updatePermit(permit: PermitEntity)

    @Query("UPDATE permits SET status = :status, approvedBy = :approvedBy WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, approvedBy: String)

    @Query("SELECT COUNT(*) FROM permits")
    suspend fun getCount(): Int
}
