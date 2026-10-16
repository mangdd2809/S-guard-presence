package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "overtimes")
data class OvertimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guardId: String,
    val guardName: String,
    val guardGroup: String,
    val guardRegion: String,
    val date: String,
    val shiftTitle: String, // "Pengawalan VIP", "Back-up Pos Timur", "Piket Malam Tambahan"
    val startTime: String, // "15:00"
    val endTime: String, // "19:00"
    val durationHours: Double,
    val reason: String,
    val status: String = "MENUNGGU", // "MENUNGGU", "DISETUJUI", "DITOLAK"
    val approvedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
