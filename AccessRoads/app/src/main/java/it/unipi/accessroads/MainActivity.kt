package it.unipi.accessroads

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import it.unipi.accessroads.databinding.ActivityMainBinding
import it.unipi.accessroads.model.AccessibilityPoint
import it.unipi.accessroads.sensors.SemanticDetectionListener
import it.unipi.accessroads.sensors.SemanticDetector
import it.unipi.accessroads.sensors.SemanticType


class MainActivity : AppCompatActivity(), SemanticDetectionListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var semanticDetector: SemanticDetector
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        semanticDetector=SemanticDetector(this)
        semanticDetector.setSemanticDetectionListener(this)
        semanticDetector.startDetection()
    }

    //
    @SuppressLint("MissingPermission")
    override fun onSemanticEventDetected(semanticInfo: SemanticType) {

        Toast.makeText(this, "now pos$semanticInfo",Toast.LENGTH_SHORT).show()
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if(location!=null){
                    val latLng=LatLng(location.latitude,location.longitude)
                    insertSemantic(latLng,semanticInfo)
                }else{
                }
            }
    }
    private fun insertSemantic(latLng: LatLng,semanticInfo: SemanticType) {
        // Use the latLng value as needed
        // For example, you can update UI elements, perform calculations, or trigger other actions
        // Example:
        Log.d("handleLocationUpdate", "Received location update - lat: ${latLng.latitude}, long: ${latLng.longitude} semtic ${semanticInfo}")
        var type = "elevator"
        if (semanticInfo == SemanticType.ROUGH_ROAD){
            type = "rough rode"
        }
        val point = AccessibilityPoint("", latLng, 0, type)
        Db.postPoint(point)
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
    override fun onResume() {
        super.onResume()
        // Start SemanticDetector
        semanticDetector.startDetection()
    }

    override fun onPause() {
        super.onPause()
        // Stop SemanticDetector
        semanticDetector.stopDetection()
    }

    override fun onDestroy() {
        super.onDestroy()
        semanticDetector.stopDetection()
    }

}