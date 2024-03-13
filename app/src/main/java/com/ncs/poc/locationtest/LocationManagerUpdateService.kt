package com.ncs.poc.locationtest

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LocationManagerUpdateService : Service(), LocationListener {

    private var locationManager: LocationManager? = null
    private var intervalSeconds = 3 //3 Seconds
    private var locationRefreshDistance = 5f //5 meters

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForegroundService()
        getLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("location_service_channel", "Location Service Channel")
        } else {
            ""
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText("Getting location updates")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        return channelId
    }

    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            intervalSeconds * 1000L,
            locationRefreshDistance,
            this
        )
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            intervalSeconds * 1000L,
            locationRefreshDistance,
            this
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationManager?.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                intervalSeconds * 1000L,
                locationRefreshDistance,
                this
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        val intent = Intent("ACTION_LOCATION_UPDATED")
        intent.putExtra("LOCATION", location)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String) {
        val intent = Intent("ACTION_PROVIDER_CHANGED")
        intent.putExtra("PROVIDER", provider)
        intent.putExtra("ENABLED", true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onProviderDisabled(provider: String) {
        val intent = Intent("ACTION_PROVIDER_CHANGED")
        intent.putExtra("PROVIDER", provider)
        intent.putExtra("ENABLED", false)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(this)
    }
}