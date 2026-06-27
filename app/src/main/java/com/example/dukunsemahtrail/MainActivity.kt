package com.example.dukunsemahtrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.dukunsemahtrail.data.AppDatabase
import com.example.dukunsemahtrail.data.TrailRepository
import com.example.dukunsemahtrail.navigation.HikeTracking
import com.example.dukunsemahtrail.navigation.TrailCatalog
import com.example.dukunsemahtrail.navigation.TrailDetail
import com.example.dukunsemahtrail.ui.catalog.TrailCatalogScreen
import com.example.dukunsemahtrail.ui.catalog.TrailCatalogViewModel
import com.example.dukunsemahtrail.ui.theme.DukunSemahTrailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = TrailRepository(database.trailDao(), database.hikeDao())

        setContent {
            DukunSemahTrailTheme {

                MainApp(repository)
            }
        }
    }
}

@Composable
fun MainApp(repository: TrailRepository) {
    val backStack = rememberNavBackStack(startKey = TrailCatalog)

    NavDisplay(
        backstack = backStack,
        onBack = { backStack.removeAt(backStack.size - 1) },
        modifier = Modifier.fillMaxSize(),
        entryProvider = entryProvider {
            entry<TrailCatalog> {
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
            entry<TrailDetail> { key ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Trail Detail for ID: ${key.trailId}")
                }
            }
            entry<HikeTracking> { key ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Hike Tracking for ID: ${key.trailId}")
                }
            }
        }
    )
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
