package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permits")
data class PermitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guardId: String,
    val guardName: String,
    val guardGroup: String,
    val permitType: String, // "Cuti Tahunan", "Izin Sakit", "Izin Kepentingan Mendesak", "Tukar Shift"
    val startDate: String,
    val endDate: String,
    val reason: String,
    val replacementGuard: String = "",
    val attachmentDesc: String = "",
    val status: String = "MENUNGGU", // "MENUNGGU", "DISETUJUI", "DITOLAK"
    val approvedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
