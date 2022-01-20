package com.example.foregroundlocationsample.utils

import android.content.Context
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
}