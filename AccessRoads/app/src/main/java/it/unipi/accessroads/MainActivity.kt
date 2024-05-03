package it.unipi.accessroads

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import it.unipi.accessroads.databinding.ActivityMainBinding
import it.unipi.accessroads.sensors.SemanticDetectionListener
import it.unipi.accessroads.sensors.SemanticDetector


class MainActivity : AppCompatActivity(), SemanticDetectionListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var semanticDetector: SemanticDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        semanticDetector=SemanticDetector(this)
        semanticDetector.setSemanticDetectionListener(this)
        semanticDetector.startDetection()
    }

    override fun onSemanticEventDetected(semanticInfo: String) {
        Toast.makeText(this, "now pos$semanticInfo",Toast.LENGTH_SHORT).show()
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