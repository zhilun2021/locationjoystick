package com.locationjoystick.feature.routes.impl

import android.content.Context
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locationjoystick.core.data.RouteRepository
import com.locationjoystick.core.model.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

private const val TAG = "RoutesViewModel"

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
) : ViewModel() {

    val uiState: StateFlow<RoutesUiState> = routeRepository.getRoutes()
        .map { routes -> RoutesUiState(routes = routes, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoutesUiState(isLoading = true)
        )

    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            routeRepository.deleteRoute(routeId)
        }
    }

    fun renameRoute(routeId: String, newName: String) {
        viewModelScope.launch {
            val route = uiState.value.routes.find { it.id == routeId } ?: return@launch
            routeRepository.updateRoute(
                route.copy(name = newName, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun exportRouteAsGpx(context: Context, route: Route) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gpx = buildGpxString(route)
                val dir = context.getExternalFilesDir("gpx") ?: return@launch
                dir.mkdirs()
                val filename = route.name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "")
                val file = File(dir, "$filename.gpx")
                file.writeText(gpx)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/gpx+xml"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(android.content.Intent.createChooser(intent, "Share GPX"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Export GPX failed", e)
            }
        }
    }

    private fun buildGpxString(route: Route): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<gpx version="1.1" creator="locationjoystick">""")
        appendLine("  <trk><name>${route.name}</name><trkseg>")
        route.waypoints.forEach { wp ->
            appendLine("""    <trkpt lat="${wp.position.latitude}" lon="${wp.position.longitude}"/>""")
        }
        appendLine("  </trkseg></trk>")
        append("</gpx>")
    }
}
