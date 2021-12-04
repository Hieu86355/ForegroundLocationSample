package com.example.foregroundlocationsample.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foregroundlocationsample.MainActivity
import com.example.foregroundlocationsample.R
import com.example.foregroundlocationsample.events.NewLocationEvent
import com.example.foregroundlocationsample.events.StopServiceEvent
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

class SampleLocationService: Service() {

    private val localBinder = LocalBinder()
    private lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(60)
            fastestInterval = TimeUnit.SECONDS.toMillis(30)
            maxWaitTime = TimeUnit.SECONDS.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(currentLocation)
                )
                EventBus.getDefault().post(NewLocationEvent(locationResult.lastLocation))
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val isCancelService = intent.getBooleanExtra(EXTRA_CANCEL_SERVICE, false)
        if (isCancelService) {
            EventBus.getDefault().post(StopServiceEvent())
            unsubcribeLocationUpdates()
            stopForeground(true)
            stopSelf()
        } else {
            val notification = createNotification(currentLocation)
            startForeground(NOTIFICATION_ID, notification)
            subscribeLocationUpdates()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return localBinder
    }

    @SuppressLint("MissingPermission")
    fun subscribeLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e(TAG, "SecurityException: ${e.message}")
        }
    }

    fun unsubcribeLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "SecurityException: ${e.message}")
        }
    }

    private fun createNotification(location: Location?): Notification {
        val locationText = "${location?.latitude} - ${location?.longitude}"
        val title = "Your current location"

        // create notification channel for android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                title,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val style = NotificationCompat
            .BigTextStyle()
            .bigText(locationText)
            .setBigContentTitle(title)

        val cancelIntent = Intent(this, SampleLocationService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_SERVICE, true)

        val servicePendingIntent = PendingIntent.getService(
            this,0,cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0
        )

        val notificationBuilder = NotificationCompat
            .Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setStyle(style)
            .setContentTitle(title)
            .setContentText(locationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(activityPendingIntent)
            .addAction(R.drawable.ic_baseline_stop_24, "Stop service", servicePendingIntent)

        return notificationBuilder.build()
    }

    inner class LocalBinder: Binder() {
        fun getBindServiceInstance(): SampleLocationService {
            return this@SampleLocationService
        }
    }

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_CHANNEL_ID = "sample_location"
        private const val NOTIFICATION_ID = 86355
        private const val EXTRA_CANCEL_SERVICE = "EXTRA_CANCEL_SERVICE"
    }
}