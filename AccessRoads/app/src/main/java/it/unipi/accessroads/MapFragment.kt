package it.unipi.accessroads

import it.unipi.accessroads.sensors.GPSManager
import it.unipi.accessroads.sensors.LocationResultListener
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import it.unipi.accessroads.databinding.FragmentMapBinding
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager
import it.unipi.accessroads.model.AccessibilityPoint
import it.unipi.accessroads.utils.Db
import it.unipi.accessroads.utils.MarkerInfoWindowAdapter
import it.unipi.accessroads.utils.PointRenderer

class MapFragment : Fragment(), LocationResultListener {

    private var _binding: FragmentMapBinding? = null
    private lateinit var gps: GPSManager
    private val locationPermissionCode = 2
    private var googleMap: GoogleMap? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        gps = GPSManager(requireContext())
        requestLocationPermission()

        binding.reportFragmentBtn.setOnClickListener {
            findNavController().navigate(R.id.action_MapFragment_to_ReportFragment)
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            addClusteredMarkers(map)
            gps.startLocationUpdates()
            updateCameraToLastKnownLocation()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            gps.startLocationUpdates()
        }
    }

    private fun updateCameraToLastKnownLocation() {
        gps.getLastKnownLocation(object : LocationResultListener {
            override fun onLocationResult(location: MutableMap<String, Double>) {
                val latitude = location["lat"]
                val longitude = location["long"]
                if (latitude != null && longitude != null) {
                    val position = LatLng(latitude, longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 18f))
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        gps.stopLocationUpdates()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gps.startLocationUpdates()
                updateCameraToLastKnownLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLocationResult(location: MutableMap<String, Double>) {
        val latitude = location["lat"]
        val longitude = location["long"]
        if (latitude != null && longitude != null) {
            val position = LatLng(latitude, longitude)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 18f))
        }
    }

    private fun addClusteredMarkers(map: GoogleMap) {
        val clusterManager = ClusterManager<AccessibilityPoint>(requireContext(), map)
        clusterManager.renderer = PointRenderer(requireContext(), map, clusterManager)
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))

        Db.getPoints { accessibilityPoints -> clusterManager.addItems(accessibilityPoints)
            clusterManager.cluster()}


        map.setOnCameraIdleListener { clusterManager.onCameraIdle() }
    }
}
