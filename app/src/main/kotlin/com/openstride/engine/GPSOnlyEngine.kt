package com.openstride.engine

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import com.openstride.data.model.TrackPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Implementation of TrackingEngine that uses only GPS data.
 */
class GPSOnlyEngine(private val context: Context) : TrackingEngine {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationUpdates = MutableSharedFlow<TrackPoint>(extraBufferCapacity = 10)
    override val locationUpdates: SharedFlow<TrackPoint> = _locationUpdates.asSharedFlow()

    private var intervalMillis: Long = 1000L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val trackPoint = TrackPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = location.time,
                    accuracy = location.accuracy,
                    speed = if (location.hasSpeed()) location.speed else null,
                    sessionId = "" // To be filled by the Service
                )
                _locationUpdates.tryEmit(trackPoint)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startTracking() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun setUpdateInterval(intervalMillis: Long) {
        this.intervalMillis = intervalMillis
    }
}
