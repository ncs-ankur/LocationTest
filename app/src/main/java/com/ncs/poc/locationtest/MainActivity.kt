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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

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

            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
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
            refreshGPSLocation()
        }
        btnRefreshNetworkLocation.setOnClickListener {
            refreshNetworkLocation()
        }

        locationUtils = LocationUtils(this@MainActivity)

        enableLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        disableLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        onGPSLocationStatusChange(locationUtils.checkProviderEnabled(LocationManager.GPS_PROVIDER))
        onNetworkLocationStatusChange(locationUtils.checkProviderEnabled(LocationManager.NETWORK_PROVIDER))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            onFusedLocationStatusChange(locationUtils.checkProviderEnabled(LocationManager.FUSED_PROVIDER))
        } else {
            onFusedLocationStatusChange(false)
        }
    }

    private fun onGPSLocationStatusChange(isEnabled: Boolean) {
        if (isEnabled) {
            txtGPSLocationStatus.setPositiveText("Enabled")
        } else {
            txtGPSLocationStatus.setNegativeText("Disabled")
        }
    }

    private fun onNetworkLocationStatusChange(isEnabled: Boolean) {
        if (isEnabled) {
            txtNetworkLocationStatus.setPositiveText("Enabled")
        } else {
            txtNetworkLocationStatus.setNegativeText("Disabled")
        }
    }

    private fun onFusedLocationStatusChange(isEnabled: Boolean) {
        if (isEnabled) {
            txtFusedLocationStatus.setPositiveText("Enabled")
        } else {
            txtFusedLocationStatus.setNegativeText("Disabled")
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

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
                intent?.getParcelableExtra<Location?>("LOCATION")
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

    private val providerStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            var provider = intent?.getStringExtra("PROVIDER")
            var enabled = intent?.getBooleanExtra("ENABLED", false) ?: false

            when (provider) {
                LocationManager.GPS_PROVIDER -> {
                    onGPSLocationStatusChange(enabled)
                }

                LocationManager.NETWORK_PROVIDER -> {
                    onNetworkLocationStatusChange(enabled)
                }

                else -> {
                    onFusedLocationStatusChange(enabled)
                }
            }
        }
    }

    private fun enableLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
            return
        }

        LocalBroadcastManager.getInstance(this@MainActivity)
            .registerReceiver(providerStatusReceiver, IntentFilter("ACTION_PROVIDER_CHANGED"))

        LocalBroadcastManager.getInstance(this@MainActivity)
            .registerReceiver(locationReceiver, IntentFilter("ACTION_LOCATION_UPDATED"))

        val intent = Intent(this, LocationManagerUpdateService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun disableLocationUpdates() {
        LocalBroadcastManager.getInstance(this@MainActivity)
            .unregisterReceiver(locationReceiver)
        LocalBroadcastManager.getInstance(this@MainActivity)
            .unregisterReceiver(providerStatusReceiver)
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