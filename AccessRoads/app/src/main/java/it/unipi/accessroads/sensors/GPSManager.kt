package it.unipi.accessroads.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.widget.Toast
import com.google.android.gms.location.*

interface LocationResultListener {
    fun onLocationResult(location: MutableMap<String, Double>)
}

class GPSManager(private val context: Context) {
    private var locationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: MutableMap<String, Double>? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
            setMinUpdateDistanceMeters(20F)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    lastLocation = mutableMapOf("lat" to latitude, "long" to longitude)
                }
            }
        }

        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(listener: LocationResultListener) {
        if (lastLocation != null) {
            listener.onLocationResult(lastLocation!!)
        } else {
            locationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val result = mutableMapOf("lat" to latitude, "long" to longitude)
                    lastLocation = result
                    listener.onLocationResult(result)
                } else {
                    Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
