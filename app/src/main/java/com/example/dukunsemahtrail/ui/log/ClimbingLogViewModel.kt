package com.example.dukunsemahtrail.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukunsemahtrail.data.HikeDao
import com.example.dukunsemahtrail.data.HikeSession
import com.example.dukunsemahtrail.data.Trail
import com.example.dukunsemahtrail.data.TrailRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LogSummary(
    val totalDistanceKm: Double,
    val totalElevationM: Int,
    val totalSessions: Int
)

data class HikeWithTrail(
    val session: HikeSession,
    val trail: Trail?
)

data class ClimbingLogUiState(
    val summary: LogSummary = LogSummary(0.0, 0, 0),
    val pastHikes: List<HikeWithTrail> = emptyList()
)

class ClimbingLogViewModel(
    private val hikeDao: HikeDao,
    private val trailRepository: TrailRepository
) : ViewModel() {

    val uiState: StateFlow<ClimbingLogUiState> = combine(
        hikeDao.getAllSessions(),
        trailRepository.allTrails
    ) { sessions, trails ->
        val finishedSessions = sessions.filter { it.status != "Active" }
        
        val summary = LogSummary(
            totalDistanceKm = finishedSessions.sumOf { it.currentDistanceKm },
            totalElevationM = finishedSessions.sumOf { it.currentElevationGainMeters },
            totalSessions = finishedSessions.size
        )

        val pastHikes = finishedSessions.map { session ->
            HikeWithTrail(
                session = session,
                trail = trails.find { it.id == session.trailId }
            )
        }

        ClimbingLogUiState(summary, pastHikes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClimbingLogUiState()
    )
}
