package com.ncs.poc.locationtest

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build

class LocationUtils(context: Context, val listener: AppLocationListener) {

    private var intervalSeconds = 3 //3 Seconds
    private var locationRefreshDistance = 5f //5 meters

    private var mLocationManager: LocationManager

    private var gpsLocationListener =
        LocationListener { location -> listener.onGPSLocationChanged(location) }

    private var networkLocationListener =
        LocationListener { location -> listener.onNetworkLocationChanged(location) }

    private var fusedLocationListener =
        LocationListener { location -> listener.onFusedLocationChanged(location) }

    init {
        mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @SuppressLint("MissingPermission")
    fun enableLocationUpdates() {
        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            intervalSeconds * 1000L,
            locationRefreshDistance,
            gpsLocationListener
        )
        mLocationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            intervalSeconds * 1000L,
            locationRefreshDistance,
            networkLocationListener
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mLocationManager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                intervalSeconds * 1000L,
                locationRefreshDistance,
                fusedLocationListener
            )
        }
    }

    fun disableLocationUpdates() {
        mLocationManager.removeUpdates(gpsLocationListener)
        mLocationManager.removeUpdates(networkLocationListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mLocationManager.removeUpdates(fusedLocationListener)
        }
    }

    interface AppLocationListener {
        fun onGPSLocationChanged(location: Location)
        fun onNetworkLocationChanged(location: Location)
        fun onFusedLocationChanged(location: Location)
    }
}