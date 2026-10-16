package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances ORDER BY clockInTimestamp DESC")
    fun getAllAttendances(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE date = :date ORDER BY clockInTimestamp DESC")
    fun getAttendancesByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE guardId = :guardId ORDER BY clockInTimestamp DESC")
    fun getAttendancesByGuard(guardId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE guardId = :guardId AND date = :date AND clockOutTimestamp IS NULL ORDER BY clockInTimestamp DESC LIMIT 1")
    suspend fun getActiveShiftAttendance(guardId: String, date: String): AttendanceEntity?

    @Query("SELECT * FROM attendances WHERE guardId = :guardId AND clockOutTimestamp IS NULL ORDER BY clockInTimestamp DESC LIMIT 1")
    suspend fun getLatestActiveAttendance(guardId: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("SELECT COUNT(*) FROM attendances")
    suspend fun getCount(): Int
}
