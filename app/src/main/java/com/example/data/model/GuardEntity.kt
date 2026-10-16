package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guards")
data class GuardEntity(
    @PrimaryKey val id: String, // NIP/ID Petugas (e.g. SEC-001)
    val name: String,
    val role: String, // "Chief Security", "Komandan Regu (Danru)", "Wakil Danru", "Petugas Pos Utama", "Petugas Patroli"
    val region: String, // "Gedung Utama & Lobby", "Zona Pabrik & Logistik", "Gerbang Timur & Parkir", "Perimeter Barat"
    val group: String, // "Regu A (Garuda)", "Regu B (Rajawali)", "Regu C (Elang)", "Regu D (Alap-alap)"
    val phone: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
