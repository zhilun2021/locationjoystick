package com.locationjoystick.core.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.model.MockLocationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MockLocationService : Service() {

    companion object {
        private const val TAG = "MockLocationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_spoof_channel"
        private const val UPDATE_INTERVAL_MS = 1000L
        private const val LOCATION_ACCURACY = 3.0f

        const val ACTION_STOP = "com.locationjoystick.core.location.ACTION_STOP"
    }

    inner class LocalBinder : Binder() {
        fun getService(): MockLocationService = this@MockLocationService
    }

    private val binder = LocalBinder()

    @Inject
    lateinit var locationManager: LocationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var updateJob: Job? = null

    private val _state = MutableStateFlow(MockLocationState.IDLE)
    val state: StateFlow<MockLocationState> = _state.asStateFlow()

    @Volatile private var currentLat: Double = 0.0
    @Volatile private var currentLon: Double = 0.0
    @Volatile private var currentSpeedMs: Float = 0.0f
    @Volatile private var currentBearing: Float = 0.0f

    private var providerAdded = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSpoofing()
            stopSelf()
            return START_NOT_STICKY
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        stopSpoofing()
        serviceScope.cancel()
        super.onDestroy()
    }

    fun startSpoofing(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon
        currentSpeedMs = 0.0f
        currentBearing = 0.0f

        setupTestProvider()
        startUpdateLoop()
        _state.value = MockLocationState.RUNNING
        Log.i(TAG, "Spoofing started at ($lat, $lon)")
    }

    fun updatePosition(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon
    }

    fun updatePositionWithVector(lat: Double, lon: Double, speedMs: Float, bearing: Float) {
        currentLat = lat
        currentLon = lon
        currentSpeedMs = speedMs
        currentBearing = bearing
    }

    fun stopSpoofing() {
        updateJob?.cancel()
        updateJob = null
        removeTestProvider()
        _state.value = MockLocationState.IDLE
        Log.i(TAG, "Spoofing stopped")
    }

    fun getCurrentPosition(): LatLng = LatLng(currentLat, currentLon)

    private fun setupTestProvider() {
        if (providerAdded) return
        try {
            val properties = ProviderProperties.Builder()
                .setHasAltitudeSupport(false)
                .setHasSpeedSupport(true)
                .setHasBearingSupport(true)
                .setPowerUsage(ProviderProperties.POWER_USAGE_HIGH)
                .setAccuracy(ProviderProperties.ACCURACY_FINE)
                .build()

            locationManager.addTestProvider(
                LocationManager.GPS_PROVIDER,
                properties,
            )
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            providerAdded = true
            Log.i(TAG, "Test provider registered")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to add test provider — another app may hold the slot", e)
            _state.value = MockLocationState.ERROR
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing mock location permission", e)
            _state.value = MockLocationState.ERROR
        }
    }

    private fun removeTestProvider() {
        if (!providerAdded) return
        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            providerAdded = false
            Log.i(TAG, "Test provider removed")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Test provider was already removed", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error removing test provider", e)
        }
    }

    private fun startUpdateLoop() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                pushLocationUpdate()
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun pushLocationUpdate() {
        if (!providerAdded) return
        try {
            val location = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = currentLat
                longitude = currentLon
                altitude = 0.0
                accuracy = LOCATION_ACCURACY
                speed = currentSpeedMs
                bearing = currentBearing
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to push location update", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error pushing location update", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Spoofing",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Active while mock location is running"
            setShowBadge(false)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openAppIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.let { intent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }

        val stopIntent = Intent(this, MockLocationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mock location active")
            .setContentText("locationjoystick is spoofing your GPS position")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(openAppIntent)
            .addAction(
                android.R.drawable.ic_delete,
                "Stop",
                stopPendingIntent,
            )
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}
