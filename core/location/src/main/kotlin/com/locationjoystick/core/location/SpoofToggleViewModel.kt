package com.locationjoystick.core.location

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.locationjoystick.core.common.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

private const val TAG = "SpoofToggleViewModel"

/**
 * Thin Hilt ViewModel wrapping [MapController.isSpoofing] / [MapController.toggleSpoofing] so any
 * screen's top bar can drive the global start/stop spoofing state without each feature's own
 * ViewModel needing to depend on [MapController] directly.
 *
 * Also reverse-geocodes the current position via Nominatim and exposes [locationLabel] so the
 * "Start" button can show a hint like "Start · Paris, France".
 */
@HiltViewModel
class SpoofToggleViewModel
    @Inject
    constructor(
        private val mapController: MapController,
    ) : ViewModel() {
        val isSpoofing: StateFlow<Boolean> = mapController.isSpoofing

        private val _locationLabel = MutableStateFlow<String?>(null)
        val locationLabel: StateFlow<String?> = _locationLabel.asStateFlow()

        init {
            viewModelScope.launch {
                mapController.sharedState
                    .map { it.currentPosition }
                    .distinctUntilChanged { old, new ->
                        if (old == null && new == null) true
                        else if (old == null || new == null) false
                        else Math.round(old.latitude * 100) == Math.round(new.latitude * 100) &&
                            Math.round(old.longitude * 100) == Math.round(new.longitude * 100)
                    }
                    .collect { pos ->
                        if (pos != null) {
                            _locationLabel.value = reverseGeocode(pos.latitude, pos.longitude)
                        }
                    }
            }
        }

        fun toggle() {
            mapController.toggleSpoofing()
        }

        private suspend fun reverseGeocode(
            lat: Double,
            lon: Double,
        ): String? =
            withContext(Dispatchers.IO) {
                try {
                    val url = URL("${AppConstants.NominatimConstants.REVERSE_URL}?lat=$lat&lon=$lon&format=json")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.setRequestProperty("User-Agent", "locationjoystick/1.0")
                    conn.connectTimeout = AppConstants.NominatimConstants.CONNECT_TIMEOUT_MS
                    conn.readTimeout = AppConstants.NominatimConstants.READ_TIMEOUT_MS
                    try {
                        val json = JSONObject(conn.inputStream.bufferedReader().readText())
                        val address = json.optJSONObject("address") ?: return@withContext null
                        val locality =
                            address.optString("city").takeIf { it.isNotEmpty() }
                                ?: address.optString("town").takeIf { it.isNotEmpty() }
                                ?: address.optString("village").takeIf { it.isNotEmpty() }
                                ?: address.optString("municipality").takeIf { it.isNotEmpty() }
                                ?: return@withContext null
                        val country = address.optString("country").takeIf { it.isNotEmpty() }
                            ?: return@withContext locality
                        "$locality, $country"
                    } finally {
                        conn.disconnect()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Reverse geocode failed", e)
                    null
                }
            }
    }

data class SpoofToggleState(
    val isSpoofing: Boolean,
    val onToggle: () -> Unit,
    val locationLabel: String? = null,
)

/**
 * Collects the global start/stop spoofing state from [SpoofToggleViewModel] so every screen's
 * `LjScaffold` call site doesn't need to repeat `hiltViewModel()` + `collectAsStateWithLifecycle()`.
 */
@Composable
fun rememberSpoofToggleState(): SpoofToggleState {
    val viewModel: SpoofToggleViewModel = hiltViewModel()
    val isSpoofing by viewModel.isSpoofing.collectAsStateWithLifecycle()
    val locationLabel by viewModel.locationLabel.collectAsStateWithLifecycle()
    return SpoofToggleState(isSpoofing, viewModel::toggle, locationLabel)
}
