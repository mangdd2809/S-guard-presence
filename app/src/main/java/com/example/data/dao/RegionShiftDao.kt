package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RegionShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionShiftDao {
    @Query("SELECT * FROM region_shifts ORDER BY region ASC, startTime ASC")
    fun getAllRegionShifts(): Flow<List<RegionShiftEntity>>

    @Query("SELECT * FROM region_shifts WHERE region = :region ORDER BY startTime ASC")
    fun getShiftsForRegion(region: String): Flow<List<RegionShiftEntity>>

    @Query("SELECT * FROM region_shifts WHERE region = :region ORDER BY startTime ASC")
    suspend fun getShiftsForRegionSync(region: String): List<RegionShiftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegionShift(shift: RegionShiftEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegionShifts(shifts: List<RegionShiftEntity>)

    @Update
    suspend fun updateRegionShift(shift: RegionShiftEntity)

    @Delete
    suspend fun deleteRegionShift(shift: RegionShiftEntity)

    @Query("DELETE FROM region_shifts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM region_shifts")
    suspend fun getCount(): Int
}
