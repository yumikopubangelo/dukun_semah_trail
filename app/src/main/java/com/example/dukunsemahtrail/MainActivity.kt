package com.example.dukunsemahtrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.dukunsemahtrail.data.AppDatabase
import com.example.dukunsemahtrail.data.HikeDao
import com.example.dukunsemahtrail.data.TrailRepository
import com.example.dukunsemahtrail.navigation.HikeTracking
import com.example.dukunsemahtrail.navigation.TrailCatalog
import com.example.dukunsemahtrail.navigation.TrailDetail
import com.example.dukunsemahtrail.navigation.ClimbingLog
import com.example.dukunsemahtrail.ui.catalog.TrailCatalogScreen
import com.example.dukunsemahtrail.ui.catalog.TrailCatalogViewModel
import com.example.dukunsemahtrail.ui.hike.ActiveHikeScreen
import com.example.dukunsemahtrail.ui.hike.ActiveHikeViewModel
import com.example.dukunsemahtrail.ui.log.ClimbingLogScreen
import com.example.dukunsemahtrail.ui.log.ClimbingLogViewModel
import com.example.dukunsemahtrail.ui.theme.DukunSemahTrailTheme
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = TrailRepository(database.trailDao(), database.hikeDao())

        setContent {
            DukunSemahTrailTheme {
                MainApp(repository, database.hikeDao())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainApp(repository: TrailRepository, hikeDao: HikeDao) {
    val backStack = rememberNavBackStack(TrailCatalog)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    
    val currentDestination = backStack.last()
    val isTracking = currentDestination is HikeTracking

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (!isTracking) {
                item(
                    selected = currentDestination is TrailCatalog || currentDestination is TrailDetail,
                    icon = { Icon(Icons.Rounded.Explore, contentDescription = null) },
                    label = { Text("Explore") },
                    onClick = {
                        if (backStack.contains(TrailCatalog)) {
                            while (backStack.last() != TrailCatalog) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        } else {
                            backStack.add(TrailCatalog)
                            while (backStack.size > 1) {
                                backStack.removeAt(0)
                            }
                        }
                    }
                )
                item(
                    selected = currentDestination is ClimbingLog,
                    icon = { Icon(Icons.Rounded.History, contentDescription = null) },
                    label = { Text("Log") },
                    onClick = {
                        if (!backStack.contains(ClimbingLog)) {
                            backStack.add(ClimbingLog)
                            while (backStack.size > 1) {
                                backStack.removeAt(0)
                            }
                        }
                    }
                )
            }
        }
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
            modifier = Modifier.fillMaxSize(),
            sceneStrategy = listDetailStrategy,
            entryProvider = entryProvider {
                entry<TrailCatalog>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Select a trail to see details",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                ) {
                    val viewModel: TrailCatalogViewModel = viewModel(
                        factory = TrailCatalogViewModelFactory(repository)
                    )
                    TrailCatalogScreen(
                        viewModel = viewModel,
                        onTrailClick = { trailId ->
                            backStack.add(TrailDetail(trailId))
                        }
                    )
                }
                entry<TrailDetail>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { key ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Trail Detail for ID: ${key.trailId}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                entry<HikeTracking>(
                    metadata = ListDetailSceneStrategy.extraPane()
                ) { key ->
                    val viewModel: ActiveHikeViewModel = viewModel(
                        key = "HikeTracking_${key.trailId}",
                        factory = ActiveHikeViewModelFactory(key.trailId, repository, hikeDao)
                    )
                    ActiveHikeScreen(
                        viewModel = viewModel,
                        onClose = {
                            backStack.removeAt(backStack.size - 1)
                        }
                    )
                }
                entry<ClimbingLog> {
                    val viewModel: ClimbingLogViewModel = viewModel(
                        factory = ClimbingLogViewModelFactory(hikeDao, repository)
                    )
                    ClimbingLogScreen(viewModel = viewModel)
                }
            }
        )
    }
}

class TrailCatalogViewModelFactory(private val repository: TrailRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrailCatalogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrailCatalogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ActiveHikeViewModelFactory(
    private val trailId: Long,
    private val repository: TrailRepository,
    private val hikeDao: HikeDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveHikeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActiveHikeViewModel(trailId, repository, hikeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ClimbingLogViewModelFactory(
    private val hikeDao: HikeDao,
    private val trailRepository: TrailRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClimbingLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClimbingLogViewModel(hikeDao, trailRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
