package com.locationjoystick.core.routing

import android.util.Log
import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.model.SyncPositionUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FollowerSyncClient"

@Singleton
class FollowerSyncClient
    @Inject
    constructor() {
        private val client =
            OkHttpClient
                .Builder()
                .connectTimeout(AppConstants.SyncConstants.POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(AppConstants.SyncConstants.POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .callTimeout(AppConstants.SyncConstants.POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build()

        private var pollJob: Job? = null
        private var lastSeq: Long = -1L

        fun startPolling(
            host: String,
            port: Int,
            groupId: String,
            onPosition: (lat: Double, lon: Double) -> Unit,
        ) {
            stopPolling()
            lastSeq = -1L
            pollJob =
                CoroutineScope(Dispatchers.IO).launch {
                    while (isActive) {
                        try {
                            val update = fetchPosition(host, port, groupId)
                            if (update != null) {
                                val nowMs = System.currentTimeMillis()
                                val stale = nowMs - update.timestamp > AppConstants.SyncConstants.POSITION_STALE_THRESHOLD_MS
                                if (!stale && update.seq > lastSeq) {
                                    lastSeq = update.seq
                                    onPosition(update.latitude, update.longitude)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Poll failed", e)
                        }
                        delay(AppConstants.SyncConstants.POLL_INTERVAL_MS)
                    }
                }
            Log.i(TAG, "Polling started: $host:$port")
        }

        fun stopPolling() {
            pollJob?.cancel()
            pollJob = null
            Log.i(TAG, "Polling stopped")
        }

        private fun fetchPosition(
            host: String,
            port: Int,
            groupId: String,
        ): SyncPositionUpdate? {
            val url = "http://$host:$port/position?token=$groupId"
            val request =
                Request
                    .Builder()
                    .url(url)
                    .get()
                    .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Poll returned ${response.code}")
                    return null
                }
                val body = response.body?.string() ?: return null
                return parsePosition(body)
            }
        }

        private fun parsePosition(json: String): SyncPositionUpdate? =
            try {
                val obj = JSONObject(json)
                SyncPositionUpdate(
                    timestamp = obj.getLong("ts"),
                    latitude = obj.getDouble("lat"),
                    longitude = obj.getDouble("lon"),
                    speedMs = obj.optDouble("speedMs", 0.0).toFloat(),
                    bearing = obj.optDouble("bearing", 0.0).toFloat(),
                    seq = obj.getLong("seq"),
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse position JSON", e)
                null
            }
    }
