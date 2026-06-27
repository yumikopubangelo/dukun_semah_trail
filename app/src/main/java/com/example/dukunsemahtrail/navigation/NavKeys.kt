package com.example.dukunsemahtrail.navigation

import androidx.navigation3.NavKey
import kotlinx.serialization.Serializable

sealed interface DukunSemahNavKey : NavKey

@Serializable
data object TrailCatalog : DukunSemahNavKey

@Serializable
data class TrailDetail(val trailId: Long) : DukunSemahNavKey

@Serializable
data class HikeTracking(val trailId: Long) : DukunSemahNavKey
