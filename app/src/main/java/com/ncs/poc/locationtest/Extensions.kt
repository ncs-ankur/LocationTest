package com.ncs.poc.locationtest

import android.content.BroadcastReceiver
import android.content.ContextWrapper
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

fun ContextWrapper.registerPublicReceiver(
    receiver: BroadcastReceiver?,
    intentFilter: IntentFilter
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, intentFilter, AppCompatActivity.RECEIVER_EXPORTED)
    } else {
        registerReceiver(receiver, intentFilter)
    }
}

fun ContextWrapper.unregisterPublicReceiver(
    receiver: BroadcastReceiver?,
) {
    receiver?.let { unregisterReceiver(it) }
}

fun Location?.getDMSFormatString(): String {
    if (this != null) {
        fun convert(degrees: Double): String {
            val degree = degrees.toInt()
            val minutesFloat = (degrees - degree) * 60
            val minutes = minutesFloat.toInt()
            val secondsFloat = (minutesFloat - minutes) * 60
            val seconds = secondsFloat.toInt()
            return String.format("%d°%d'%d\"", degree, minutes, seconds)
        }

        val latitudeRef = if (latitude >= 0) "N" else "S"
        val longitudeRef = if (longitude >= 0) "E" else "W"

        val latitudeDMS = convert(Math.abs(latitude)) + latitudeRef
        val longitudeDMS = convert(Math.abs(longitude)) + longitudeRef

        return "$latitudeDMS, $longitudeDMS"
    } else {
        return "null"
    }
}

fun TextView.setPositiveText(strText: String) {
    text = strText
    setTextColor(context.getColor(R.color.colorPositive))
}

fun TextView.setNegativeText(strText: String) {
    text = strText
    setTextColor(context.getColor(R.color.colorNegative))
}

fun Location?.getAccuracyString(): String {
    if (this != null) {
        return String.format("Accuracy %.2f meters", accuracy)
    } else {
        return "null"
    }
}