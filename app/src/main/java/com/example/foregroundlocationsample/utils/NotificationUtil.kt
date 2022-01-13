package com.example.foregroundlocationsample.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.foregroundlocationsample.MainActivity
import com.example.foregroundlocationsample.R

object NotificationUtil {

    @SuppressLint("UnspecifiedImmutableFlag")
    fun createNotification(context: Context): Notification {
        val style = NotificationCompat
            .BigTextStyle()
            .setBigContentTitle(Constants.NOTIFICATION_TITLE)
            .bigText(Constants.NOTIFICATION_CONTENT_TEXT)

        val launchActivityIntent = Intent(context, MainActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        val activityPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context, 0, launchActivityIntent, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context, 0, launchActivityIntent, 0
            )
        }
        val notificationBuilder = NotificationCompat
            .Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setStyle(style)
            .setContentTitle(Constants.NOTIFICATION_TITLE)
            .setContentText(Constants.NOTIFICATION_CONTENT_TEXT)
            .setSmallIcon(R.drawable.ic_round_location_on_24)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(activityPendingIntent)

        return notificationBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(notificationManager: NotificationManager) {
        val notificationChannel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }
}