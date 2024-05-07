package it.unipi.accessroads.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class Gps(private val fragment: Fragment, private val locationListener: LocationListener) {

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    fun getLocation() {
        val context = fragment.requireContext()
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(fragment.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, locationListener)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permesso concesso, richiedi la posizione
                getLocation()
            } else {
                // Permesso negato, gestisci di conseguenza
            }
        }
    }

    fun getLastKnownLocation(): Location? {
        val context = fragment.requireContext()
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else {
            null
        }
    }
}
