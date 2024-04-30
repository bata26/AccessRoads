package it.unipi.accessroads

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import it.unipi.accessroads.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    /*
    private val accessPoints: List<AccessPoint> by lazy {
        PlacesReader(this).read()
    }
    */

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reportFragmentBtn.setOnClickListener {
            findNavController().navigate(R.id.action_MapFragment_to_ReportFragment)
        }
        binding.debugPage.setOnClickListener {
            findNavController().navigate(R.id.action_MapFragment_to_DebugFragment)
        }
        binding.sensorStartStop.setOnClickListener {
            val context: Context? = getContext()
            Toast.makeText(context, "Sensors", Toast.LENGTH_SHORT).show()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->

            // Add markers
            //addClusteredMarkers(googleMap)
            //addMarkers(googleMap)

            // Wait for map to finish loading
            googleMap.setOnMapLoadedCallback {
                // Ensure all places are visible in the map
                val bounds = LatLngBounds.builder()
                val latitude = 43.717 // Example latitude value
                val longitude = 10.383 // Example longitude value
                val position = LatLng(latitude, longitude)
                bounds.include(position)
                //accessPoints.forEach { bounds.include(it.latLng) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
/*
    private fun addMarkers(googleMap: GoogleMap) {
        accessPoints.forEach { point ->
            val marker = googleMap.addMarker {
                title(point.name)
                position(point.latLng)
                icon(point.icon)
            }
            // Set place as the tag on the marker object so it can be referenced within
            // MarkerInfoWindowAdapter
            marker?.tag = point
        }
    }
    */
}