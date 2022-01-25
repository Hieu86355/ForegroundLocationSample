package com.example.foregroundlocationsample.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.location.*
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.foregroundlocationsample.models.RawGnssData
import com.example.foregroundlocationsample.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.foregroundlocationsample.utils.Constants.ACTION_STOP_SERVICE
import com.example.foregroundlocationsample.utils.Constants.FASTED_LOCATION_INTERVAL
import com.example.foregroundlocationsample.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.example.foregroundlocationsample.utils.Constants.NOTIFICATION_ID
import com.example.foregroundlocationsample.utils.NotificationUtil.createNotification
import com.example.foregroundlocationsample.utils.NotificationUtil.createNotificationChannel
import com.example.foregroundlocationsample.utils.PermissionUtil
import com.example.foregroundlocationsample.utils.Util
import com.example.foregroundlocationsample.utils.WriteXML
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.location.lbs.gnss.gps.pseudorange.PseudorangePositionVelocityFromRealTimeEvents


private const val TAG = "TrackingLocationService"
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingLocationService : LifecycleService() {
    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val currentGnssStatus = MutableLiveData<GnssStatus>()
        val currentRawGnssData = MutableLiveData<RawGnssData>()
        val mPseudorangePositionVelocityFromRealTimeEvents = PseudorangePositionVelocityFromRealTimeEvents()
    }

    var isWritedToObject = false
    val rawGNSSList = mutableListOf<RawGnssData>()
    lateinit var mThread: HandlerThread
    lateinit var mHandler: Handler

    var isForegroundServiceRunning = false

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    lateinit var locationManager: LocationManager

    var gnssStatus : GnssStatus? = null
    var rawGnssData: RawGnssData? = null

    val gnssCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            gnssStatus = status
        }
    }

    val rawGnssCallback = object: GnssMeasurementsEvent.Callback() {
        override fun onGnssMeasurementsReceived(eventArgs: GnssMeasurementsEvent?) {
            rawGnssData = RawGnssData(eventArgs)
            if (!isWritedToObject) {
                rawGNSSList.add(RawGnssData(eventArgs))
                Log.d("hieu", "add raw measurement to list")
            }
            val r = Runnable {
                mPseudorangePositionVelocityFromRealTimeEvents
                    .computePositionVelocitySolutionsFromRawMeas(eventArgs)
            }
            val posSolution =
                TrackingLocationService.mPseudorangePositionVelocityFromRealTimeEvents.positionSolutionLatLngDeg
            if (posSolution[0].isNaN()) {
                mHandler.post(r)
                //Log.d("hieu", "onGnssMeasurementsReceived: ${rawGNSSList.size}")
            }
//            else if (!isWritedToObject) {
//                isWritedToObject = true
//                //Util.writeObjectToFile(rawGNSSList, this@TrackingLocationService)
//                WriteXML.createGnssXML(rawGNSSList, this@TrackingLocationService)
//            }
        }

    }

    val navigationMessageCallback = object: GnssNavigationMessage.Callback() {
        override fun onGnssNavigationMessageReceived(event: GnssNavigationMessage?) {
            mPseudorangePositionVelocityFromRealTimeEvents.parseHwNavigationMessageUpdates(event)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        Log.d(TAG, "New location: ${location.latitude} : ${location.longitude}")
                        //Toast.makeText(this@TrackingLocationService,"${location.latitude} : ${location.longitude} : ${location.altitude}", Toast.LENGTH_LONG).show()

                        addPathPoint(location)
                    }
                }
            }
            mPseudorangePositionVelocityFromRealTimeEvents.setReferencePosition(
                (result.lastLocation.latitude * 1E7).toInt(),
                (result.lastLocation.longitude * 1E7).toInt(),
                (result.lastLocation.altitude * 1E7).toInt()
            )
            gnssStatus?.let {
                currentGnssStatus.postValue(it)
            }
            rawGnssData?.let {
                currentRawGnssData.postValue(it)
            }
        }
    }

    val nmeaMessageListener = object : OnNmeaMessageListener {
        override fun onNmeaMessage(message: String?, timestamp: Long) {
            if (message != null && message.contains("GGA", true) && !isWritedToObject) {
                val parts = message.split(",");
                if (parts[2].isNotEmpty() && parts[3].isNotEmpty() && parts[4].isNotEmpty() && parts[5].isNotEmpty()) {
                    isWritedToObject = true
                    WriteXML.createGnssXML(rawGNSSList, this@TrackingLocationService)
                    Log.d("hieu", "onNmeaMessage: $parts")
                }
            }
        }
    }


    override fun onCreate() {
        super.onCreate()

        initialValues()

        // notify updates whenever isTracking state changes
        isTracking.observe(this, Observer {
            isForegroundServiceRunning = it
            updateLocationTracking(it)
        })

        mThread = HandlerThread("test")
        mThread.start()
        mHandler = Handler(mThread.looper)

    }

    private fun initialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
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
                // Request location update
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTED_LOCATION_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    // the smallest displacement in meters the user must move between location updates.
                    //smallestDisplacement = 5.0f
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )

                // Register location gnss status
                locationManager.registerGnssStatusCallback(gnssCallback, Handler(Looper.getMainLooper()))
                locationManager.registerGnssMeasurementsCallback(rawGnssCallback, Handler(Looper.getMainLooper()))
                locationManager.registerGnssNavigationMessageCallback(navigationMessageCallback, Handler(Looper.getMainLooper()))
                locationManager.addNmeaListener(nmeaMessageListener, Handler(Looper.getMainLooper()))

                if (rawGNSSList.size > 0) {
                    rawGNSSList.clear()
                }
            }
        } else {
            // unregister location update callback
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)

            // unregister gnss status callback
            locationManager.unregisterGnssStatusCallback(gnssCallback)

            // unregister raw gnss measurement callback
            locationManager.unregisterGnssMeasurementsCallback(rawGnssCallback)

            locationManager.unregisterGnssNavigationMessageCallback(navigationMessageCallback)
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
        isTracking.postValue(false)
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