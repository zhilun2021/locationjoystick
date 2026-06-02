package com.locationjoystick.feature.widget.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.locationjoystick.core.location.MockLocationService
import com.locationjoystick.core.overlay.OverlayServiceHelper
import com.locationjoystick.feature.joystick.impl.JoystickOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the [ServiceConnection]s and bound-service references used by [FloatingWidgetService].
 *
 * Binds to three services:
 * - [MockLocationService] (via [OverlayServiceHelper.bindTrackedService] — torn down by
 *   [OverlayServiceHelper.cleanupOverlayBindings])
 * - [JoystickOverlayService] (direct bind/unbind)
 * - [ElevationOverlayService] (direct bind/unbind)
 *
 * The bound services are exposed as nullable properties. The host service calls [bind] from
 * `onCreate` and [unbind] from `onDestroy`.
 */
internal class WidgetServiceBinder(
    private val context: Context,
    private val serviceScope: CoroutineScope,
    private val overlayHelper: OverlayServiceHelper,
    private val joystickVisibleFlow: MutableStateFlow<Boolean>,
    private val joystickLockedFlow: MutableStateFlow<Boolean>,
    private val elevationOverlayVisibleFlow: MutableStateFlow<Boolean>,
) {
    companion object {
        private const val TAG = "FloatingWidgetService"
    }

    var mockLocationService: MockLocationService? = null
        private set

    var joystickService: JoystickOverlayService? = null
        private set

    var elevationOverlayService: ElevationOverlayService? = null
        private set

    private var joystickBound = false
    private var elevationOverlayBound = false

    private val mockLocationServiceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName,
                binder: IBinder,
            ) {
                mockLocationService = (binder as MockLocationService.LocalBinder).getService()
                Log.d(TAG, "Bound to MockLocationService")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mockLocationService = null
                Log.d(TAG, "Unbound from MockLocationService")
            }
        }

    private val joystickConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName,
                binder: IBinder,
            ) {
                val svc = (binder as JoystickOverlayService.LocalBinder).getService()
                joystickService = svc
                serviceScope.launch { svc.isVisible.collect { joystickVisibleFlow.value = it } }
                serviceScope.launch { svc.isLocked.collect { joystickLockedFlow.value = it } }
                Log.d(TAG, "Bound to JoystickOverlayService")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                joystickService = null
                joystickBound = false
                joystickVisibleFlow.value = false
                joystickLockedFlow.value = false
                Log.d(TAG, "Unbound from JoystickOverlayService")
            }
        }

    private val elevationOverlayConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName,
                binder: IBinder,
            ) {
                elevationOverlayService = (binder as ElevationOverlayService.LocalBinder).getService()
                Log.d(TAG, "Bound to ElevationOverlayService")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                elevationOverlayService = null
                elevationOverlayBound = false
                elevationOverlayVisibleFlow.value = false
                Log.d(TAG, "Unbound from ElevationOverlayService")
            }
        }

    fun bind() {
        overlayHelper.bindTrackedService(context, Intent(context, MockLocationService::class.java), mockLocationServiceConnection)
        val joystickIntent =
            Intent().apply {
                setClassName(context.packageName, "com.locationjoystick.feature.joystick.impl.JoystickOverlayService")
            }
        joystickBound = context.bindService(joystickIntent, joystickConnection, Context.BIND_AUTO_CREATE)
        elevationOverlayBound =
            context.bindService(
                Intent(context, ElevationOverlayService::class.java),
                elevationOverlayConnection,
                Context.BIND_AUTO_CREATE,
            )
    }

    fun unbind() {
        if (joystickBound) {
            try {
                context.unbindService(joystickConnection)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Joystick service was not bound when attempting to unbind", e)
            }
        }
        if (elevationOverlayBound) {
            try {
                context.unbindService(elevationOverlayConnection)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Elevation overlay service was not bound when attempting to unbind", e)
            }
        }
    }
}
