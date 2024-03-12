package com.ncs.poc.locationtest

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ncs.poc.locationtest.receivers.LocationEnabledReceiver
import com.ncs.poc.locationtest.receivers.NetworkChangeReceiver

class MainActivity : AppCompatActivity(), LocationEnabledReceiver.LocationReceiverListener,
    NetworkChangeReceiver.ConnectivityReceiverListener, LocationUtils.AppLocationListener {

    private lateinit var txtGPSLocationStatus: TextView
    private lateinit var txtGPSLocation: TextView
    private lateinit var txtNetworkStatus: TextView
    private lateinit var txtNetworkLocation: TextView
    private lateinit var txtNetworkLocationStatus: TextView
    private lateinit var txtFusedLocationStatus: TextView
    private lateinit var txtFusedLocation: TextView
    private lateinit var txtGPSAccuracy: TextView
    private lateinit var txtNetworkAccuracy: TextView
    private lateinit var txtFusedAccuracy: TextView

    private lateinit var locationUtils: LocationUtils

    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var locationEnabledReceiver: LocationEnabledReceiver

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                enableLocationUpdates()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                enableLocationUpdates()
            }

            else -> {
                Toast.makeText(this@MainActivity, "Location permission denied!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtGPSLocationStatus = findViewById(R.id.txtGPSLocationStatus)
        txtGPSLocation = findViewById(R.id.txtGPSLocation)
        txtNetworkStatus = findViewById(R.id.txtNetworkStatus)
        txtNetworkLocation = findViewById(R.id.txtNetworkLocation)
        txtNetworkLocationStatus = findViewById(R.id.txtNetworkLocationStatus)
        txtFusedLocationStatus = findViewById(R.id.txtFusedLocationStatus)
        txtFusedLocation = findViewById(R.id.txtFusedLocation)

        txtGPSAccuracy = findViewById(R.id.txtGPSAccuracy)
        txtNetworkAccuracy = findViewById(R.id.txtNetworkAccuracy)
        txtFusedAccuracy = findViewById(R.id.txtFusedAccuracy)

        locationUtils = LocationUtils(this@MainActivity, this)

        registerReceiver()
        enableLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
        disableLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        onGPSLocationStatusChange(locationEnabledReceiver.checkGPSLocationEnabled(this@MainActivity))
        onNetworkLocationStatusChange(locationEnabledReceiver.checkNetworkLocationEnabled(this@MainActivity))
        onFusedLocationStatusChange(locationEnabledReceiver.checkFusedLocationEnabled(this@MainActivity))
        onNetworkConnectionChanged(networkChangeReceiver.isConnectionAvailable(this@MainActivity))
    }

    override fun onGPSLocationStatusChange(isEnabled: Boolean) {
        if (isEnabled) {
            txtGPSLocationStatus.setPositiveText("Enabled")
        } else {
            txtGPSLocationStatus.setNegativeText("Disabled")
        }
    }

    override fun onNetworkLocationStatusChange(isEnabled: Boolean) {
        if (isEnabled) {
            txtNetworkLocationStatus.setPositiveText("Enabled")
        } else {
            txtNetworkLocationStatus.setNegativeText("Disabled")
        }
    }

    override fun onFusedLocationStatusChange(isEnabled: Boolean) {
        if (isEnabled) {
            txtFusedLocationStatus.setPositiveText("Enabled")
        } else {
            txtFusedLocationStatus.setNegativeText("Disabled")
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            txtNetworkStatus.setPositiveText("Connected")
        } else {
            txtNetworkStatus.setNegativeText("Disconnected")
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun registerReceiver() {
        networkChangeReceiver = NetworkChangeReceiver(this)
        locationEnabledReceiver = LocationEnabledReceiver(this)

        val filterConnectivityChange = IntentFilter()
        val action = ConnectivityManager.CONNECTIVITY_ACTION
        filterConnectivityChange.addAction(action)
        registerPublicReceiver(
            networkChangeReceiver,
            filterConnectivityChange,
        )
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        registerPublicReceiver(
            locationEnabledReceiver,
            filter
        )
    }

    private fun unregisterReceivers() {
        unregisterPublicReceiver(networkChangeReceiver)
        unregisterPublicReceiver(locationEnabledReceiver)
    }

    private fun enableLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        locationUtils.enableLocationUpdates()
    }

    private fun disableLocationUpdates() {
        locationUtils.disableLocationUpdates()
    }

    override fun onGPSLocationChanged(location: Location) {
        txtGPSLocation.text = location.getDMSFormatString()
        txtGPSAccuracy.text = location.getAccuracyString()
    }

    override fun onNetworkLocationChanged(location: Location) {
        txtNetworkLocation.text = location.getDMSFormatString()
        txtNetworkAccuracy.text = location.getAccuracyString()
    }

    override fun onFusedLocationChanged(location: Location) {
        txtFusedLocation.text = location.getDMSFormatString()
        txtFusedAccuracy.text = location.getAccuracyString()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
}