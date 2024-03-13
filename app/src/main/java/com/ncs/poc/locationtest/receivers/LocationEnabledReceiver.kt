package com.ncs.poc.locationtest.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.ncs.poc.locationtest.LocationUtils

class LocationEnabledReceiver(
    private val locationReceiverListener: LocationReceiverListener
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent!!.getAction())) {
            locationReceiverListener.onGPSLocationStatusChange(LocationUtils.checkGPSLocationEnabled(context))
            locationReceiverListener.onNetworkLocationStatusChange(
                LocationUtils.checkNetworkLocationEnabled(
                    context
                )
            )
            locationReceiverListener.onFusedLocationStatusChange(LocationUtils.checkFusedLocationEnabled(context))
        }
    }

    interface LocationReceiverListener {
        fun onGPSLocationStatusChange(isEnabled: Boolean)
        fun onNetworkLocationStatusChange(isEnabled: Boolean)
        fun onFusedLocationStatusChange(isEnabled: Boolean)
    }
}