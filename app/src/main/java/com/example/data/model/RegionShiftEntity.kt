package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region_shifts")
data class RegionShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val region: String, // "Jalancagak", "Sagalaherang", "Wanareja"
    val shiftName: String, // "Shift 1", "Shift 2", "Shift 3", "Shift Non-Shift"
    val startTime: String, // "06:00"
    val endTime: String, // "14:00"
    val toleranceMinutes: Int = 15,
    val description: String = ""
) {
    val displayLabel: String
        get() = "$shiftName : $startTime - $endTime"
}
