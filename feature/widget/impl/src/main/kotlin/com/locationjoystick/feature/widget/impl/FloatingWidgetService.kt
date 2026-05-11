package com.locationjoystick.feature.widget.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.locationjoystick.core.data.LocationRepository
import com.locationjoystick.core.data.RouteRepository
import com.locationjoystick.core.location.MockLocationService
import com.locationjoystick.core.overlay.OverlayService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FloatingWidgetService : OverlayService(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        private const val TAG = "FloatingWidgetService"
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    @Inject lateinit var routeRepository: RouteRepository
    @Inject lateinit var locationRepository: LocationRepository

    private var routePickerPopup: RoutePickerPopup? = null

    private var mockLocationService: MockLocationService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            mockLocationService = (binder as MockLocationService.LocalBinder).getService()
            Log.d(TAG, "Bound to MockLocationService")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mockLocationService = null
            Log.d(TAG, "Unbound from MockLocationService")
        }
    }

    override fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        bindService(
            Intent(this, MockLocationService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        routePickerPopup?.dismiss()
        routePickerPopup = null
        try {
            unbindService(serviceConnection)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Service was not bound when attempting to unbind", e)
        }
        super.onDestroy()
    }

    override fun createOverlayView(): View {
        return Button(this).apply {
            text = "Routes"
            setOnClickListener { toggleRoutePicker() }
        }
    }

    private fun toggleRoutePicker() {
        val popup = routePickerPopup
        if (popup != null) {
            popup.dismiss()
            routePickerPopup = null
        } else {
            val newPopup = RoutePickerPopup(
                context = this,
                windowManager = windowManager,
                lifecycleOwner = this,
                savedStateRegistryOwner = this,
                routeRepository = routeRepository,
                onRouteSelected = { route, isBackward ->
                    startRouteReplay(route.id, isBackward)
                    routePickerPopup?.dismiss()
                    routePickerPopup = null
                },
                onDismiss = {
                    routePickerPopup = null
                },
            )
            newPopup.show()
            routePickerPopup = newPopup
        }
    }

    private fun startRouteReplay(routeId: String, isBackward: Boolean) {
        val intent = Intent(this, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_ROUTE_REPLAY_START
            putExtra(MockLocationService.EXTRA_ROUTE_ID, routeId)
            putExtra(MockLocationService.EXTRA_IS_BACKWARD, isBackward)
            putExtra(MockLocationService.EXTRA_SPEED_MS, 1.4)
        }
        startService(intent)
    }
}
