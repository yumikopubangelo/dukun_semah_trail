package com.example.dukunsemahtrail.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trails")
data class Trail(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,
    val difficulty: String,
    val distanceKm: Double,
    val elevationGainMeters: Int,
    val description: String,
    val terrainType: String,
    val safetyWarnings: String,
    val imageUrl: String? = null
)
