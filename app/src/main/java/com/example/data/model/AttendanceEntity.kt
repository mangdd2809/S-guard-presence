package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guardId: String,
    val guardName: String,
    val guardRole: String,
    val guardGroup: String,
    val guardRegion: String,
    val date: String, // Format "yyyy-MM-dd"
    val shiftType: String, // "Shift Pagi (07:00 - 15:00)", "Shift Siang (15:00 - 23:00)", "Shift Malam (23:00 - 07:00)"
    val clockInTime: String, // "06:55:12"
    val clockInTimestamp: Long = System.currentTimeMillis(),
    val clockOutTime: String? = null,
    val clockOutTimestamp: Long? = null,
    val status: String, // "TEPAT_WAKTU", "TERLAMBAT", "LEMBUR"
    val locationName: String, // "Pos Gerbang Utama", "Pos Lobby Gedung A", etc.
    val notes: String = "",
    val isVerified: Boolean = true
)
