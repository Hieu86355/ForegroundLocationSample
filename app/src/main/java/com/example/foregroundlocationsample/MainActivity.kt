package com.example.foregroundlocationsample

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.foregroundlocationsample.databinding.ActivityMainBinding
import com.example.foregroundlocationsample.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.foregroundlocationsample.utils.Constants.ACTION_STOP_SERVICE
import com.example.foregroundlocationsample.utils.Constants.PERMISSIONS_REQUEST_CODE
import com.example.foregroundlocationsample.services.Polyline
import com.example.foregroundlocationsample.services.TrackingLocationService
import com.example.foregroundlocationsample.utils.PermissionUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.location.lbs.gnss.gps.pseudorange.PseudorangePositionVelocityFromRealTimeEvents

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var map: GoogleMap? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mMapview.onCreate(savedInstanceState)
        binding.mMapview.getMapAsync {
            map = it
            addAllPolylines()
        }

        binding.btnStartStop.setOnClickListener {
            if (PermissionUtil.isLocationPermissionApproved(this)) {
                toggleTracking()
            } else {
                PermissionUtil.requestLocationPermissions(this)
            }
        }

        initBottomSheet()

        subscribeObservers()
    }

    private fun initBottomSheet() {
        val mBottomSheet = BottomSheetBehavior.from(binding.bottomSheet.bottomSheetRootLayout)
        binding.bottomSheet.bottomSheetRootLayout.setOnClickListener {
            if (mBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            } else if (mBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
                mBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        mBottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> { // Expanded
                        binding.btnStartStop.hide()
                        mBottomSheet.isDraggable = false
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> { // Collapsed
                        binding.btnStartStop.show()
                        mBottomSheet.isDraggable = true
                    }

                }
            }
        })
        // Add scrollable for textview
        binding.bottomSheet.txtGnssStatus.movementMethod = ScrollingMovementMethod()
    }

    private fun subscribeObservers() {
        TrackingLocationService.isTracking.observe(this, Observer { isTracking ->
            updateTracking(isTracking)
        })

        TrackingLocationService.pathPoints.observe(this, Observer { polylines ->
            pathPoints = polylines
            addLatestPolyline()
            moveCameraToCurrentLocation()
        })

        /*TrackingLocationService.currentGnssStatus.observe(this, Observer {
            var list = mutableListOf<String>()
            for (consIndex in 0..(it.satelliteCount - 1)) {
                val type = it.getConstellationType(consIndex)
                list.add(getConstellationType(type))
            }
            var displayList = ""
            for (consType in list) {
                displayList += "\n Constellation Type :: $consType"
            }
            displayList += "\n--------------------------"

            binding.bottomSheet.txtGnssStatus.append(displayList)

        })*/

        TrackingLocationService.currentRawGnssData.observe(this, Observer {
            binding.bottomSheet.txtGnssStatus.append(it.toString())
            val posSolution =
                TrackingLocationService.mPseudorangePositionVelocityFromRealTimeEvents.positionSolutionLatLngDeg
            Toast.makeText(this, "${posSolution[0]} - ${posSolution[1]}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun getConstellationType(type: Int): String {
        return when(type) {
            0 -> "UNKNOWN"
            1 -> "GPS"
            2 -> "SBAS"
            3 -> "GLONASS"
            4 -> "QZSS"
            5 -> "BEIDOU"
            6 -> "GALILEO"
            7 -> "IRNSS"
            else -> "UNKNOWN"
        }
    }

    private fun toggleTracking() {
        if (isTracking) {
            sendCommandToService(ACTION_STOP_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        setButtonResource(isTracking)
    }

    private fun setButtonResource(isTracking: Boolean) {
        val btnResource = if (isTracking) R.drawable.ic_baseline_stop_24 else R.drawable.ic_baseline_play_arrow_24
        binding.btnStartStop.setImageResource(btnResource)
    }

    private fun moveCameraToCurrentLocation() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), 20f)
            )
        }
    }

    // Connect all polyline on the map incase configuration changes
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(Color.RED)
                .width(8f)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    // Connect the last point of the list to the new points
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            // Last second element inside of the last list
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]

            val lastLatLng = pathPoints.last().last()

            val polylineOptions = PolylineOptions()
                .color(Color.RED)
                .width(8f)
                .add(preLastLatLng)
                .add(lastLatLng)

            val markerOptions = MarkerOptions()
                .position(lastLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                .alpha(0.7f)

            map?.addPolyline(polylineOptions)
            map?.addMarker(markerOptions)
        }
    }

    private fun sendCommandToService(action: String) {
        Intent(this, TrackingLocationService::class.java).also {
            it.action = action
            startService(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            when {
                // If user interaction was interrupted, the permission request
                // is cancelled and you receive empty arrays.
                grantResults.isEmpty() -> Unit

                // Permission was granted.
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    toggleTracking()
                }

                else -> {
                    // Permission denied.
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mMapview.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mMapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mMapview.onStop()
    }

    override fun onStop() {
        super.onStop()
        binding.mMapview.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mMapview.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mMapview.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mMapview.onSaveInstanceState(outState)
    }
}