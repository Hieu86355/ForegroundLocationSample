package com.example.foregroundlocationsample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.example.foregroundlocationsample.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode

import org.greenrobot.eventbus.Subscribe

private const val PERMISSIONS_REQUEST_CODE = 1234

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EventBus.getDefault().register(this)

        // add scrollable textview
        binding.locationTxt.movementMethod = ScrollingMovementMethod()

        val switchSetting = getSharedPreferences("SWITCH", MODE_PRIVATE)
        val isChecked = switchSetting.getBoolean("CHECKED_STATE", false)
        binding.locationSwitch.isChecked = isChecked

        binding.locationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            switchSetting.edit {
                this.putBoolean("CHECKED_STATE", isChecked)
                this.commit()
            }
            when (isChecked) {
                true -> {
                    if (permissionApproved()) {
                        startService(Intent(this, LocationService::class.java))
                    } else {
                        requestPermissions()
                    }
                }

                false -> {
                    stopService(Intent(this, LocationService::class.java))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewLocationEvent) {
        val mLocation = event.location
        binding.locationTxt.append("\nLocation: (${mLocation.latitude} - ${mLocation.longitude})")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: StopServiceEvent) {
        binding.locationSwitch.isChecked = false
    }

    private fun permissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val provideRationale = permissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(binding.root, "Permission denined!!!", Snackbar.LENGTH_LONG)
                .setAction("OK") {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_CODE
            )
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
                    startService(Intent(this, LocationService::class.java))
                }

                else -> {
                    // Permission denied.
                    Snackbar.make(binding.root, "Permission denined!!!", Snackbar.LENGTH_LONG)
                        .setAction("OK") {
                            // Request permission
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                PERMISSIONS_REQUEST_CODE
                            )
                        }
                        .show()
                }
            }
        }
    }

}