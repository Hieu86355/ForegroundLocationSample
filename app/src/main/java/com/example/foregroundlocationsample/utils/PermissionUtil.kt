package com.example.foregroundlocationsample.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.foregroundlocationsample.utils.Constants.PERMISSIONS_REQUEST_CODE

object PermissionUtil {

    fun requestLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_CODE
        )
    }

    fun isLocationPermissionApproved(context: Context): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}

/*// If the user denied a previous request, but didn't check "Don't ask again", provide
                    // additional rationale.
                    Snackbar.make(binding.root, "Permission denined!!!", Snackbar.LENGTH_LONG)
                        .setAction("OK") {
                            // Request permission
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                PERMISSIONS_REQUEST_CODE
                            )
                        }
                        .show()*/