package com.example.dukunsemahtrail.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hike_sessions")
data class HikeSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trailId: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val currentDistanceKm: Double = 0.0,
    val currentElevationGainMeters: Int = 0,
    val status: String = "Active"
)
