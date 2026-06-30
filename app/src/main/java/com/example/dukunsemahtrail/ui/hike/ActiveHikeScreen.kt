package com.example.dukunsemahtrail.ui.hike

import android.Manifest
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dukunsemahtrail.service.LocationService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActiveHikeScreen(
    viewModel: ActiveHikeViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            val intent = Intent(context, LocationService::class.java).apply {
                action = "START"
            }
            context.startForegroundService(intent)
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    val initialPosition = remember(uiState.pathPoints) {
        if (uiState.pathPoints.isNotEmpty()) {
            Position(latitude = uiState.pathPoints[0].latitude, longitude = uiState.pathPoints[0].longitude)
        } else {
            Position(latitude = 6.075, longitude = 116.559)
        }
    }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = initialPosition,
            zoom = 12.0
        )
    )

    // Trail Source
    val trailSource = rememberGeoJsonSource(
        data = if (uiState.pathPoints.size >= 2) {
            GeoJsonData.Features(
                FeatureCollection(
                    features = listOf(
                        Feature(
                            geometry = LineString(
                                uiState.pathPoints.map { Position(latitude = it.latitude, longitude = it.longitude) }
                            ),
                            properties = JsonObject(emptyMap())
                        )
                    )
                )
            )
        } else {
            GeoJsonData.JsonString("{\"type\": \"FeatureCollection\", \"features\": []}")
        }
    )

    // User Location Source
    val userLocationSource = rememberGeoJsonSource(
        data = if (uiState.currentLocation != null) {
            GeoJsonData.Features(
                FeatureCollection(
                    features = listOf(
                        Feature(
                            geometry = Point(
                                Position(
                                    latitude = uiState.currentLocation!!.latitude,
                                    longitude = uiState.currentLocation!!.longitude
                                )
                            ),
                            properties = JsonObject(emptyMap())
                        )
                    )
                )
            )
        } else {
            GeoJsonData.JsonString("{\"type\": \"FeatureCollection\", \"features\": []}")
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.trail?.name ?: "Active Hike") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // MAP LAYER
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = BaseStyle.Uri("https://demotiles.maplibre.org/style.json"),
            ) {
                LineLayer(
                    id = "trail-layer",
                    source = trailSource,
                    color = const(Color.Green),
                    width = const(5.dp)
                )

                CircleLayer(
                    id = "user-location-layer",
                    source = userLocationSource,
                    color = const(Color.Blue),
                    radius = const(8.dp)
                )
            }

            // METRICS OVERLAY
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricOverlayCard(
                        label = "Dist",
                        value = "${uiState.distanceKm}",
                        unit = "km",
                        modifier = Modifier.weight(1f)
                    )
                    MetricOverlayCard(
                        label = "Elev",
                        value = "${uiState.elevationM}",
                        unit = "m",
                        modifier = Modifier.weight(1f)
                    )
                    MetricOverlayCard(
                        label = "Pace",
                        value = "${uiState.paceMinKm}",
                        unit = "m/k",
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val intent = Intent(context, LocationService::class.java).apply {
                            action = "STOP"
                        }
                        context.startService(intent)
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("FINISH HIKE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MetricOverlayCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
