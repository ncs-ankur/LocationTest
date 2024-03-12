package com.ncs.poc.locationtest.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log

class LocationEnabledReceiver(
    private val locationReceiverListener: LocationReceiverListener
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent!!.getAction())) {
            locationReceiverListener.onGPSLocationStatusChange(checkGPSLocationEnabled(context))
            locationReceiverListener.onNetworkLocationStatusChange(
                checkNetworkLocationEnabled(
                    context
                )
            )
            locationReceiverListener.onFusedLocationStatusChange(checkFusedLocationEnabled(context))
        }
    }

    fun checkGPSLocationEnabled(context: Context?): Boolean {
        var isEnabled = false
        context?.let {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var enabled = false
            try {
                enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
                Log.d("GPS", ex.localizedMessage)
            }
            isEnabled = enabled
        }
        return isEnabled
    }

    fun checkNetworkLocationEnabled(context: Context?): Boolean {
        var isEnabled = false
        context?.let {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var enabled = false
            try {
                enabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
                Log.d("NETWORK", ex.localizedMessage)
            }
            isEnabled = enabled
        }
        return isEnabled
    }

    fun checkFusedLocationEnabled(context: Context?): Boolean {
        var isEnabled = false
        context?.let {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var enabled = false
            try {
                enabled =
                    locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
            } catch (ex: Exception) {
                Log.d("FUSED", ex.localizedMessage)
            }
            isEnabled = enabled
        }
        return isEnabled
    }

    interface LocationReceiverListener {
        fun onGPSLocationStatusChange(isEnabled: Boolean)
        fun onNetworkLocationStatusChange(isEnabled: Boolean)
        fun onFusedLocationStatusChange(isEnabled: Boolean)
    }
}