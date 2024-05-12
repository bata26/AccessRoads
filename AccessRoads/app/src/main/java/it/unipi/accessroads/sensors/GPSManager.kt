package it.unipi.accessroads.sensors
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

interface LocationResultListener {
    fun onLocationResult(location: MutableMap<String, Double>)
}

class GPSManager (private val context: Context) {
    private lateinit var locationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(listener: LocationResultListener) {
        locationClient = LocationServices.getFusedLocationProviderClient(context)

        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val result = mutableMapOf<String, Double>()
                result["lat"] = latitude
                result["long"] = longitude
                listener.onLocationResult(result)
            } else {
                Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}