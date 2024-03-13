package com.ncs.poc.locationtest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ncs.poc.locationtest.receivers.LocationEnabledReceiver


class MainActivity : AppCompatActivity(), LocationEnabledReceiver.LocationReceiverListener {

    private lateinit var txtGPSLocationStatus: TextView
    private lateinit var txtGPSLocation: TextView
    private lateinit var txtNetworkLocation: TextView
    private lateinit var txtNetworkLocationStatus: TextView
    private lateinit var txtFusedLocationStatus: TextView
    private lateinit var txtFusedLocation: TextView
    private lateinit var txtGPSAccuracy: TextView
    private lateinit var txtNetworkAccuracy: TextView
    private lateinit var txtFusedAccuracy: TextView

    private lateinit var locationUtils: LocationUtils

    private lateinit var btnRefreshGPSLocation: View
    private lateinit var progressGPSLocation: View
    private lateinit var btnRefreshNetworkLocation: View
    private lateinit var progressNetworkLocation: View

    private lateinit var locationEnableReceiver: LocationEnabledReceiver

    private var pendingPermissionTask: (() -> Unit)? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                pendingPermissionTask?.invoke()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                pendingPermissionTask?.invoke()
            }

            else -> {
                //openLocationPermissionSetting()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtGPSLocationStatus = findViewById(R.id.txtGPSLocationStatus)
        txtGPSLocation = findViewById(R.id.txtGPSLocation)
        txtNetworkLocation = findViewById(R.id.txtNetworkLocation)
        txtNetworkLocationStatus = findViewById(R.id.txtNetworkLocationStatus)
        txtFusedLocationStatus = findViewById(R.id.txtFusedLocationStatus)
        txtFusedLocation = findViewById(R.id.txtFusedLocation)

        txtGPSAccuracy = findViewById(R.id.txtGPSAccuracy)
        txtNetworkAccuracy = findViewById(R.id.txtNetworkAccuracy)
        txtFusedAccuracy = findViewById(R.id.txtFusedAccuracy)

        btnRefreshGPSLocation = findViewById(R.id.btnRefreshGPSLocation)
        progressGPSLocation = findViewById(R.id.progressGPSLocation)
        btnRefreshNetworkLocation = findViewById(R.id.btnRefreshNetworkLocation)
        progressNetworkLocation = findViewById(R.id.progressNetworkLocation)

        btnRefreshGPSLocation.visibility = View.VISIBLE
        progressGPSLocation.visibility = View.GONE
        btnRefreshNetworkLocation.visibility = View.VISIBLE
        progressNetworkLocation.visibility = View.GONE

        btnRefreshGPSLocation.setOnClickListener {
            whenPermissionAvailable {
                refreshGPSLocation()
            }
        }
        btnRefreshNetworkLocation.setOnClickListener {
            whenPermissionAvailable {
                refreshNetworkLocation()
            }
        }

        locationUtils = LocationUtils(this@MainActivity)

        locationEnableReceiver = LocationEnabledReceiver(this@MainActivity)
        enableLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        disableLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        updateProviderStatus()
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun updateProviderStatus() {
        onGPSLocationStatusChange(locationUtils.checkProviderEnabled(LocationManager.GPS_PROVIDER))
        onNetworkLocationStatusChange(locationUtils.checkProviderEnabled(LocationManager.NETWORK_PROVIDER))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            onFusedLocationStatusChange(locationUtils.checkProviderEnabled(LocationManager.FUSED_PROVIDER))
        } else {
            onFusedLocationStatusChange(false)
        }
    }

    private fun updateLastKnownLocations() {
        onGPSLocationChanged(locationUtils.getLastKnownLocation(LocationManager.GPS_PROVIDER))
        onNetworkLocationChanged(locationUtils.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            onFusedLocationChanged(locationUtils.getLastKnownLocation(LocationManager.FUSED_PROVIDER))
        } else {
            onFusedLocationChanged(null)
        }
    }

    private fun refreshGPSLocation() {
        btnRefreshGPSLocation.visibility = View.GONE
        progressGPSLocation.visibility = View.VISIBLE
        locationUtils.refreshLocation(
            this@MainActivity,
            LocationManager.GPS_PROVIDER,
            object : LocationUtils.AppSingleLocationListener {
                override fun onLocationReceived(location: Location?) {
                    progressGPSLocation.visibility = View.GONE
                    btnRefreshGPSLocation.visibility = View.VISIBLE
                    onGPSLocationChanged(location)
                }
            })
    }

    private fun refreshNetworkLocation() {
        btnRefreshNetworkLocation.visibility = View.GONE
        progressNetworkLocation.visibility = View.VISIBLE
        locationUtils.refreshLocation(this@MainActivity,
            LocationManager.NETWORK_PROVIDER, object : LocationUtils.AppSingleLocationListener {
                override fun onLocationReceived(location: Location?) {
                    btnRefreshNetworkLocation.visibility = View.VISIBLE
                    progressNetworkLocation.visibility = View.GONE
                    onNetworkLocationChanged(location)
                }
            })
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra("LOCATION", Location::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra("LOCATION")
            }
            location?.let {
                when (it.provider) {
                    LocationManager.GPS_PROVIDER -> {
                        onGPSLocationChanged(it)
                    }

                    LocationManager.NETWORK_PROVIDER -> {
                        onNetworkLocationChanged(it)
                    }

                    else -> {
                        onFusedLocationChanged(it)
                    }
                }
            }
        }
    }

    private fun whenPermissionAvailable(theTask: (() -> Unit)?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showPermissionRationale {
                    askLocationPermission()
                }
            } else {
                askLocationPermission()
            }
            pendingPermissionTask = theTask
        } else {
            theTask?.invoke()
        }
    }

    private fun showPermissionRationale(function: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Location permission required")
            .setMessage("We seriously need location permission to use this app!")
            .setPositiveButton("Fine") { _, _ -> function.invoke() }
            .setNegativeButton("I don't care", null)
            .show()
    }

    private fun askLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }

    private fun enableLocationUpdates() {
        whenPermissionAvailable {
            updateLastKnownLocations()

            LocalBroadcastManager.getInstance(this@MainActivity)
                .registerReceiver(locationReceiver, IntentFilter("ACTION_LOCATION_UPDATED"))

            val intent = Intent(this, LocationManagerUpdateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        registerPublicReceiver(
            locationEnableReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
    }

    private fun disableLocationUpdates() {
        LocalBroadcastManager.getInstance(this@MainActivity)
            .unregisterReceiver(locationReceiver)

        unregisterPublicReceiver(locationEnableReceiver)
    }

    private fun onGPSLocationChanged(location: Location?) {
        if (location != null) {
            txtGPSLocation.setPositiveText(location.getDMSFormatString())
            txtGPSAccuracy.text = location.getAccuracyString()
        } else {
            txtGPSLocation.setNegativeText("(null)")
            txtGPSAccuracy.text = ""
        }
    }

    private fun onNetworkLocationChanged(location: Location?) {
        if (location != null) {
            txtNetworkLocation.setPositiveText(location.getDMSFormatString())
            txtNetworkAccuracy.text = location.getAccuracyString()
        } else {
            txtNetworkLocation.setNegativeText("(null)")
            txtNetworkAccuracy.text = ""
        }
    }

    private fun onFusedLocationChanged(location: Location?) {
        if (location != null) {
            txtFusedLocation.setPositiveText(location.getDMSFormatString())
            txtFusedAccuracy.text = location.getAccuracyString()
        } else {
            txtFusedLocation.setNegativeText("(null)")
            txtFusedAccuracy.text = ""
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
}