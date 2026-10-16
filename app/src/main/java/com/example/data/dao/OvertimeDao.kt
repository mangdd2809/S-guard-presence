package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OvertimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OvertimeDao {
    @Query("SELECT * FROM overtimes ORDER BY createdAt DESC")
    fun getAllOvertimes(): Flow<List<OvertimeEntity>>

    @Query("SELECT * FROM overtimes WHERE guardId = :guardId ORDER BY createdAt DESC")
    fun getOvertimesByGuard(guardId: String): Flow<List<OvertimeEntity>>

    @Query("SELECT * FROM overtimes WHERE status = :status ORDER BY createdAt DESC")
    fun getOvertimesByStatus(status: String): Flow<List<OvertimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOvertime(overtime: OvertimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOvertimes(overtimes: List<OvertimeEntity>)

    @Update
    suspend fun updateOvertime(overtime: OvertimeEntity)

    @Query("UPDATE overtimes SET status = :status, approvedBy = :approvedBy WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, approvedBy: String)

    @Query("SELECT COUNT(*) FROM overtimes")
    suspend fun getCount(): Int
}
