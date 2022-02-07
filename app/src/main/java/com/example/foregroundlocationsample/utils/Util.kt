package com.example.foregroundlocationsample.utils

import android.content.Context
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.util.Log
import com.example.foregroundlocationsample.models.RawGnssData
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.lang.Exception

object Util {
    fun writeObjectToFile(data: List<RawGnssData>, ctx: Context) {
        try {
            var dataTxt = ""
            data.forEach {
                dataTxt += it.toString()
            }

            val file = File(ctx.filesDir, "raw_gnss.txt")
            val stream = FileOutputStream(file)
            stream.write(dataTxt.toByteArray())
            stream.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun is4GpsSatellite(event: GnssMeasurementsEvent?): Boolean {
        if (event == null) {
            return false
        }
        var count = 0;
        for (measurement in event.measurements) {
            if (measurement.constellationType == GnssStatus.CONSTELLATION_GPS) {
                count++
            }
        }
        return count >= 4
    }
}