package com.locationjoystick.feature.widget.impl

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.locationjoystick.core.designsystem.LjIcons
import com.locationjoystick.core.designsystem.LjInactive
import com.locationjoystick.core.designsystem.LjTheme
import com.locationjoystick.core.designsystem.UiConstants
import com.locationjoystick.core.location.MockLocationService
import com.locationjoystick.core.model.ElevationMode
import com.locationjoystick.core.overlay.OverlayService
import com.locationjoystick.core.overlay.OverlayServiceHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class ElevationOverlayService :
    OverlayService(),
    LifecycleOwner,
    SavedStateRegistryOwner {
    companion object {
        private const val TAG = "ElevationOverlayService"
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "ElevationOverlayService coroutine crashed", throwable)
        }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)
    private val overlayHelper = OverlayServiceHelper(TAG)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val _elevationMode = MutableStateFlow<ElevationMode?>(null)
    val elevationMode: StateFlow<ElevationMode?> = _elevationMode.asStateFlow()

    private var mockLocationService: MockLocationService? = null

    private val mockServiceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName,
                binder: IBinder,
            ) {
                val svc = (binder as MockLocationService.LocalBinder).getService()
                mockLocationService = svc
                _elevationMode.value?.let { svc.setElevationMode(it) }
                Log.d(TAG, "Bound to MockLocationService")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mockLocationService = null
                Log.d(TAG, "Unbound from MockLocationService")
            }
        }

    inner class LocalBinder : Binder() {
        fun getService(): ElevationOverlayService = this@ElevationOverlayService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        overlayHelper.registerOverlayVisibilityReceiver(this, this)
        overlayHelper.bindTrackedService(this, Intent(this, MockLocationService::class.java), mockServiceConnection)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
        (overlayView as? ComposeView)?.disposeComposition()
        overlayHelper.cleanupOverlayBindings(this)
        super.onDestroy()
    }

    override fun showOverlayOnStart(): Boolean = false

    val isOverlayVisible: Boolean
        get() = overlayView?.isAttachedToWindow == true

    fun toggleOverlay() {
        if (overlayView == null) {
            val view = createOverlayView()
            overlayView = view
            currentParams = getWindowManagerParams(view)
        }
        if (isOverlayVisible) hideOverlay() else showOverlay()
    }

    fun setMode(mode: ElevationMode) {
        _elevationMode.value = mode
        mockLocationService?.setElevationMode(mode)
        Log.d(TAG, "Elevation mode set: $mode")
    }

    override fun createOverlayView(): View {
        val view =
            ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@ElevationOverlayService)
                setViewTreeSavedStateRegistryOwner(this@ElevationOverlayService)
            }
        view.setContent {
            val currentMode by elevationMode.collectAsStateWithLifecycle()
            LjTheme {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    listOf(
                        Triple(ElevationMode.TiltUp, LjIcons.ElevationUp, "Tilt up"),
                        Triple(ElevationMode.Neutral, LjIcons.ElevationNeutral, "Neutral"),
                        Triple(ElevationMode.TiltDown, LjIcons.ElevationDown, "Tilt down"),
                    ).forEach { (mode, icon, desc) ->
                        val tint = if (currentMode == mode) MaterialTheme.colorScheme.primary else LjInactive
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .size(UiConstants.FAB_CONTAINER_SIZE)
                                    .background(Color.Black, CircleShape)
                                    .clickable { setMode(mode) },
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = desc,
                                tint = tint,
                                modifier = Modifier.size(UiConstants.FAB_ICON_SIZE),
                            )
                        }
                    }
                }
            }
        }
        return view
    }
}
