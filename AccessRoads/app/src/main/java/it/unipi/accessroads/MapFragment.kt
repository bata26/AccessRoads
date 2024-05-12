package it.unipi.accessroads
import it.unipi.accessroads.sensors.*
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
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

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MapFragment : Fragment(), LocationListener{

    private var _binding: FragmentMapBinding? = null
    private lateinit var gps: GPSManager
    private val locationPermissionCode = 2

    // This property is only valid between onCreateView and
    // onDestroyView.
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
        requestLocationPermission()
        // Initialize and request GPS location
        gps = GPSManager(requireContext())
        super.onViewCreated(view, savedInstanceState)

        binding.reportFragmentBtn.setOnClickListener {
            findNavController().navigate(R.id.action_MapFragment_to_ReportFragment)
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            addClusteredMarkers(googleMap)
            // Set custom info window adapter
            gps.getLastKnownLocation(object : LocationResultListener {
                override fun onLocationResult(location: MutableMap<String, Double>) {
                    val latitude = location["lat"]
                    val longitude = location["long"]
                    val position = LatLng(latitude ?: 43.717, longitude ?: 10.383)
                    googleMap.setOnMapLoadedCallback {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))
                        println("Latitude: $latitude, Longitude: $longitude")
                    }
                }
            })
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
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLocationChanged(location: Location) {
        println("LOCATION CHANGED")
        val latitude = location.latitude
        val longitude = location.longitude
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            googleMap.setOnMapLoadedCallback {
                val position = LatLng(latitude, longitude)
                LatLngBounds.builder().include(position).build()
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // Create the ClusterManager class and set the custom renderer.
        val clusterManager = ClusterManager<AccessibilityPoint>(requireContext(), googleMap)
        clusterManager.renderer =
            PointRenderer(
                requireContext(),
                googleMap,
                clusterManager
            )

        // Set custom info window adapter
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))
        // Add the places to the ClusterManager.
        Db.getPoints { accessibilityPoints -> clusterManager.addItems(accessibilityPoints)}
        clusterManager.cluster()

        // Set ClusterManager as the OnCameraIdleListener so that it
        // can re-cluster when zooming in and out.
        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }
    }
}