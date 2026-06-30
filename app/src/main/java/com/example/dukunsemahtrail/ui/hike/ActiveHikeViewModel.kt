package com.example.dukunsemahtrail.ui.hike

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukunsemahtrail.data.HikeDao
import com.example.dukunsemahtrail.data.HikeSession
import com.example.dukunsemahtrail.data.Trail
import com.example.dukunsemahtrail.data.TrailRepository
import com.example.dukunsemahtrail.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import kotlin.math.roundToInt

data class LatLng(val latitude: Double, val longitude: Double)

data class HikeUiState(
    val trail: Trail? = null,
    val distanceKm: Double = 0.0,
    val elevationM: Int = 0,
    val paceMinKm: Double = 0.0,
    val isTracking: Boolean = false,
    val pathPoints: List<LatLng> = emptyList(),
    val currentLocation: LatLng? = null
)

class ActiveHikeViewModel(
    private val trailId: Long,
    private val repository: TrailRepository,
    private val hikeDao: HikeDao
) : ViewModel() {

    private val _trail = MutableStateFlow<Trail?>(null)
    
    private var lastLocation: Location? = null
    private var totalDistanceMeters = 0.0
    private var startTimeMillis = System.currentTimeMillis()

    val uiState: StateFlow<HikeUiState> = combine(
        _trail,
        LocationService.locationFlow,
        LocationService.isTracking
    ) { trail, location, isTracking ->
        if (location != null && isTracking) {
            updateMetrics(location)
        }
        
        val elapsedTimeMin = (System.currentTimeMillis() - startTimeMillis) / 60000.0
        val pace = if (totalDistanceMeters > 0) elapsedTimeMin / (totalDistanceMeters / 1000.0) else 0.0

        HikeUiState(
            trail = trail,
            distanceKm = (totalDistanceMeters / 1000.0 * 100.0).roundToInt() / 100.0,
            elevationM = location?.altitude?.toInt() ?: 0,
            paceMinKm = (pace * 10.0).roundToInt() / 10.0,
            isTracking = isTracking,
            pathPoints = parsePathJson(trail?.pathPointsJson),
            currentLocation = location?.let { LatLng(it.latitude, it.longitude) }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HikeUiState()
    )

    init {
        viewModelScope.launch {
            _trail.value = repository.getTrailById(trailId)
            startNewSession()
        }
    }

    private fun updateMetrics(location: Location) {
        lastLocation?.let { last ->
            totalDistanceMeters += last.distanceTo(location).toDouble()
        }
        lastLocation = location
    }

    private suspend fun startNewSession() {
        val session = HikeSession(
            trailId = trailId,
            startTime = System.currentTimeMillis()
        )
        hikeDao.insertSession(session)
    }

    private fun parsePathJson(json: String?): List<LatLng> {
        if (json == null) return emptyList()
        return try {
            val arr = JSONArray(json)
            val points = mutableListOf<LatLng>()
            for (i in 0 until arr.length()) {
                val point = arr.getJSONArray(i)
                points.add(LatLng(point.getDouble(0), point.getDouble(1)))
            }
            points
        } catch (e: Exception) {
            emptyList()
        }
    }
}
