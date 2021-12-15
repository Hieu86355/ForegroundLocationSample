package com.example.foregroundlocationsample.utils

object Constants {

    const val PERMISSIONS_REQUEST_CODE = 1234

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    const val NOTIFICATION_CHANNEL_NAME = "LocationTrackingSample"
    const val NOTIFICATION_CHANNEL_ID = "sample_tracking_location"
    const val NOTIFICATION_ID = 123456
    const val NOTIFICATION_TITLE = "Foreground Location"
    const val NOTIFICATION_CONTENT_TEXT = "Tracking your location..."

    const val START_STOP_PREFERENCE = "START_STOP_PREFERENCE"
    const val BUTTON_STATE = "BUTTON_STATE"

    const val LOCATION_UPDATE_INTERVAL = 15000L
    const val FASTED_LOCATION_INTERVAL = 10000L // minimum interval to get location update
}