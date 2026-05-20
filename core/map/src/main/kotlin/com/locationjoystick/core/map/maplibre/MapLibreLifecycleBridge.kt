package com.locationjoystick.core.map.maplibre

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.maps.MapView

/**
 * Forwards Android lifecycle events to [mapView].
 *
 * Extracted from the verbatim `DisposableEffect` block duplicated in all 4 map screens.
 * Handles the full lifecycle including `onCreate`/`onStart` on attachment.
 *
 * @param mapView The MapLibre [MapView] to forward events to.
 * @param lifecycleOwner The lifecycle owner to observe. Defaults to [LocalLifecycleOwner].
 * @param callCreateOnAttach If `true` (default), calls `mapView.onCreate(null)` and
 *   `mapView.onStart()` immediately when the effect is first attached — required when
 *   the MapView is created inside a `remember {}` block (MapScreen, MapFloatingView).
 *   Pass `false` when the MapView already receives `ON_START` via the observer
 *   (MapPickerScreen, RouteCreatorScreen pattern).
 */
@Composable
fun MapLibreLifecycleBridge(
    mapView: MapView,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    callCreateOnAttach: Boolean = true,
) {
    DisposableEffect(lifecycleOwner) {
        if (callCreateOnAttach) {
            mapView.onCreate(null)
            mapView.onStart()
        }
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> if (!callCreateOnAttach) mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> if (!callCreateOnAttach) mapView.onStop()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
}
