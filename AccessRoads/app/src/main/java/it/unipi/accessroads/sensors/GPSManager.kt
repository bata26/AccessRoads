package it.unipi.accessroads.sensors
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

interface LocationResultListener {
    fun onLocationResult(location: MutableMap<String, Double>)
}

class GPSManager (context: Context) {
    private lateinit var locationClient: FusedLocationProviderClient
    private val locationPermissionCode = 2
    private val context = context

    @SuppressLint("MissingPermission")
    fun getLocation() : MutableMap<String, Double>{
        var lat = 0.0
        var long = 0.0
        locationClient = LocationServices.getFusedLocationProviderClient(context)

        locationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    lat = location.latitude
                    long = location.longitude
                }

            }
        val res = mutableMapOf<String, Double>()
        res["lat"] = lat
        res["long"] = long

        return res

    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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