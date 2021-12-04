package com.example.foregroundlocationsample.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.foregroundlocationsample.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.foregroundlocationsample.utils.Constants.ACTION_STOP_SERVICE
import com.example.foregroundlocationsample.utils.Constants.FASTED_LOCATION_INTERVAL
import com.example.foregroundlocationsample.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.example.foregroundlocationsample.utils.Constants.NOTIFICATION_ID
import com.example.foregroundlocationsample.utils.NotificationUtil.createNotification
import com.example.foregroundlocationsample.utils.NotificationUtil.createNotificationChannel
import com.example.foregroundlocationsample.utils.PermissionUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

private const val TAG = "TrackingLocationService"
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingLocationService : LifecycleService() {
    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    var isForegroundServiceRunning = false

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        Log.d(TAG, "New location: ${location.latitude} : ${location.longitude}")
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        initialValues()

        // notify updates whenever isTracking state changes
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })

    }

    private fun initialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    // Add coordinate to the last polyline of polyline list
    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (PermissionUtil.isLocationPermissionApproved(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTED_LOCATION_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Log.d(TAG, "Start or Resume service")
                    if (!isForegroundServiceRunning) {
                        isForegroundServiceRunning = true
                        addEmptyPolyline()
                        isTracking.postValue(true) // this will trigger the updateLocationTracking
                        startForegroundService()
                    } else {
                        // Resuming service
                    }
                }

                ACTION_STOP_SERVICE -> {
                    Log.d(TAG, "Stop service")
                    stopService()
                }

                else -> Unit
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopService() {
        stopForeground(true)
        //stopSelf()
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, createNotification(applicationContext))
    }
}