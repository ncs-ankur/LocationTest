package com.ncs.poc.locationtest.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ncs.poc.locationtest.LocationUtils

class LocationEnabledReceiver(
    private val locationReceiverListener: LocationReceiverListener
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        locationReceiverListener.onGPSLocationStatusChange(LocationUtils.checkGPSLocationEnabled(context))
        locationReceiverListener.onNetworkLocationStatusChange(
            LocationUtils.checkNetworkLocationEnabled(
                context
            )
        )
        locationReceiverListener.onFusedLocationStatusChange(LocationUtils.checkFusedLocationEnabled(context))
    }

    interface LocationReceiverListener {
        fun onGPSLocationStatusChange(isEnabled: Boolean)
        fun onNetworkLocationStatusChange(isEnabled: Boolean)
        fun onFusedLocationStatusChange(isEnabled: Boolean)
    }
}