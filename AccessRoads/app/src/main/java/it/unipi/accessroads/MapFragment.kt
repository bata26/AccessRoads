package it.unipi.accessroads
import it.unipi.accessroads.sensors.Gps
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import it.unipi.accessroads.databinding.FragmentMapBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.clustering.ClusterManager
import it.unipi.accessroads.model.AccessibilityPoint
import it.unipi.accessroads.utils.PointRenderer

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MapFragment : Fragment(), LocationListener{

    private var _binding: FragmentMapBinding? = null
    private lateinit var gps: Gps
    private lateinit var points : List<AccessibilityPoint>

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

        // Inizializza e richiedi la posizione GPS
        gps = Gps(this, this)
        gps.getLocation()
        super.onViewCreated(view, savedInstanceState)

        binding.reportFragmentBtn.setOnClickListener {
            findNavController().navigate(R.id.action_MapFragment_to_ReportFragment)
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            //addMarkers(googleMap)
            addClusteredMarkers(googleMap)
            // Set custom info window adapter
            //googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))

            googleMap.setOnMapLoadedCallback {
                // Ensure all places are visible in the map
                val bounds = LatLngBounds.builder()
                val latitude = gps.getLastKnownLocation()?.latitude
                    ?: 43.717
                val longitude = gps.getLastKnownLocation()?.longitude
                    ?: 10.383
                val position = LatLng(latitude, longitude)
                bounds.include(position)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 2f))
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync({ googleMap ->
            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                val position = LatLng(latitude, longitude)
                bounds.include(position)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))
            }
        })
    }

    private val elevatorIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(requireContext(), R.drawable.ic_elevator_24, Color.parseColor("#2AAC17"))
    }

    private val roughRodeIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(requireContext(), R.drawable.ic_road_24, Color.parseColor("#B70202"))
    }

    private val stairsIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(requireContext(), R.drawable.ic_stairs_24, Color.parseColor("#545454"))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Gestisci i risultati della richiesta di permesso
        gps.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun addMarkers(googleMap: GoogleMap) {
        Db.getPoints { accessibilityPoints ->
            for (point in accessibilityPoints) {
                Log.d(TAG, "POINT : ${point}")
                val icon : BitmapDescriptor
                when (point.type) {
                    "elevator" -> {
                        icon = elevatorIcon
                    }

                    "rough rode" -> {
                        icon = roughRodeIcon
                    }

                    "stairs" -> {
                        icon = stairsIcon
                    }

                    else -> {
                        icon = BitmapDescriptorFactory.defaultMarker()
                    }
                }
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(point.position)
                        .title(point.type)
                        .icon(icon)
                )
                // Set place as the tag on the marker object so it can be referenced within
                // MarkerInfoWindowAdapter
                marker?.tag = point
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

/**
 * Adds markers to the map with clustering support.
 */


    //override fun onMapReady(googleMap: GoogleMap) {
        //val sydney = LatLng(-33.852, 151.211)
        //for (point in this.points) {
        //    googleMap.addMarker(
        //        MarkerOptions()
        //            .position(point.position)
        //            .title(point.type)
        //    )
        //}


    //    Log.d(TAG, "Accessibility Point: ${this.points}")

        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    //}
