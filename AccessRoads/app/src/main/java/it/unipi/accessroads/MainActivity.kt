package it.unipi.accessroads

import it.unipi.accessroads.sensors.*
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.maps.model.LatLng
import it.unipi.accessroads.databinding.ActivityMainBinding
import it.unipi.accessroads.model.AccessibilityPoint
import it.unipi.accessroads.sensors.SemanticDetectionListener
import it.unipi.accessroads.sensors.SemanticDetector
import it.unipi.accessroads.sensors.SemanticType
import it.unipi.accessroads.utils.Db


class MainActivity : AppCompatActivity(), SemanticDetectionListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var semanticDetector: SemanticDetector
    private lateinit var gpsManager: GPSManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        gpsManager=GPSManager(this)
        gpsManager.startLocationUpdates()
        semanticDetector=SemanticDetector(this)
        semanticDetector.setSemanticDetectionListener(this)
        semanticDetector.startDetection()
    }

    //
    @SuppressLint("MissingPermission")
    override fun onSemanticEventDetected(semanticInfo: SemanticType) {
        gpsManager.getLastKnownLocation(object :LocationResultListener{
            override fun onLocationResult(location: MutableMap<String, Double>) {
                val latitude=location["lat"]
                val longitude=location["long"]
                if(latitude!=null&&longitude!=null){
                    val latLng=LatLng(latitude, longitude)
                    insertSemantic(latLng,semanticInfo)
                }else{
                    Log.d("error","Location is null")
                }
            }
        })
    }
    private fun insertSemantic(latLng: LatLng,semanticInfo: SemanticType) {
        Log.d("handleLocationUpdate", "Received location update - lat: ${latLng.latitude}, long: ${latLng.longitude} semantic $semanticInfo")
        var type = "Elevator"
        if (semanticInfo == SemanticType.ROUGH_ROAD){
            type = "Rough Road"
        }
        val point = AccessibilityPoint(Db.getUUID(), latLng, 1, type)
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
        gpsManager.startLocationUpdates()
        semanticDetector.startDetection()

    }

    override fun onPause() {
        super.onPause()
        // Stop SemanticDetector
        semanticDetector.stopDetection()
        gpsManager.stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        semanticDetector.stopDetection()
        gpsManager.stopLocationUpdates()
    }

}