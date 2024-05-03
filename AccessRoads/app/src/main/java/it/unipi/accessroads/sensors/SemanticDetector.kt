package it.unipi.accessroads.sensors

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.hardware.*
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import it.unipi.accessroads.utils.FilterType
import kotlin.math.abs
import it.unipi.accessroads.utils.TimeSeries
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private var accelerometer:Sensor?=null
    private var barometer:Sensor?=null

    private val handler= Handler()
    private val TIME_SERIES_SIZE=100
    private val WINDOW_SIZE=24//must be a even number
    private lateinit var accelerometData:TimeSeries
    private lateinit var barometerData:TimeSeries
    private var DETECTED=false
    private var ON_PAUSE_DEACTIVATE=false
    private val ELEVATOR_BAR_CHANGE=0.15f
    private val ELEVATOR_ACC_CHANGE=0.08f
    private val ROUGH_ROAD_STDDEV=0.4f
    private var accedata= mutableStateOf("")//si puo togliere
    private var bardata= mutableStateOf("")// same
    private val barometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val pressure = event.values[0]

            barometerData.addValue(pressure)
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }
    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val accelerationMagnitude = calculateAccelerationMagnitude(event.values)

            accelerometData.addValue(accelerationMagnitude)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }
    private fun checkMovement() {
        handler.postDelayed(object :Runnable{
            override fun run() {
                if(ON_PAUSE_DEACTIVATE){
                    return
                }
                val valuesBar=barometerData.getWindowToAnalize(WINDOW_SIZE,FilterType.LOW_PASS)
                val valuesAcc=accelerometData.getWindowToAnalize(WINDOW_SIZE,FilterType.LOW_PASS)
                bardata.value="bar:${String.format("%.2f", valuesBar[0])},${String.format("%.2f", valuesBar[WINDOW_SIZE/2])},${String.format("%.2f", valuesBar[WINDOW_SIZE-1])}"
                if(valuesBar[0]>0&&abs(valuesBar[0]-valuesBar[WINDOW_SIZE-1])>=ELEVATOR_BAR_CHANGE&&abs(valuesBar[0]-valuesBar[WINDOW_SIZE-1])<5){
                    Toast.makeText(this@MainActivity,"bar changed over 0.2->"+String.format("%.2f", valuesAcc[WINDOW_SIZE-1]-valuesAcc[0]),Toast.LENGTH_SHORT).show()
                    val halfWindow=(WINDOW_SIZE/2)
                    val firstHalfWindow=halfWindow-(halfWindow/2)
                    val secondHalfWindow=halfWindow+(halfWindow/2)
                    if(abs(valuesAcc[0]-valuesAcc[firstHalfWindow])>=ELEVATOR_ACC_CHANGE||abs(valuesAcc[WINDOW_SIZE-1]-valuesAcc[secondHalfWindow])>=ELEVATOR_ACC_CHANGE){
                        Toast.makeText(this@MainActivity,"Elevator detected",Toast.LENGTH_SHORT).show()
                        DETECTED=true
                    }
                }
                if (valuesBar[0]>0&&abs(valuesBar[0]-valuesBar[WINDOW_SIZE-1])<ELEVATOR_BAR_CHANGE){
                    Toast.makeText(this@MainActivity,"stddev->"+String.format("%.2f", valuesBar[WINDOW_SIZE-1]-valuesBar[0]),Toast.LENGTH_SHORT).show()
                    if(calculateStandardDeviation(valuesAcc)>=ROUGH_ROAD_STDDEV){
                        Toast.makeText(this@MainActivity,"Rough Road Detected with "+"stddev->"+String.format("%.2f", valuesBar[WINDOW_SIZE-1]-valuesBar[0]),Toast.LENGTH_SHORT).show()
                        DETECTED=true
                    }
                }
                Log.d("stdDEV",calculateStandardDeviation(valuesAcc).toString())
                accedata.value="\nx:${String.format("%.2f", valuesAcc[0])},,\ny:${String.format("%.2f", valuesAcc[WINDOW_SIZE/2])},,\nz:${String.format("%.2f", valuesAcc[WINDOW_SIZE-1])}\n"


                if(DETECTED){
                    DETECTED=false
                    handler.postDelayed(this,8000L)
                    Toast.makeText(this@MainActivity,"sleep 8secs",Toast.LENGTH_SHORT).show()
                }else{
                    handler.postDelayed(this,4000L)
                }
            }
        },4000L)
    }
    private fun calculateStandardDeviation(values: FloatArray): Float {
        // Calculate mean
        val mean = values.average()

        // Calculate sum of squared differences from the mean
        val sumOfSquaredDifferences = values.sumOf { (it - mean).pow(2) }

        // Calculate variance
        val variance = sumOfSquaredDifferences / values.size

        // Calculate standard deviation
        return sqrt(variance).toFloat()
    }
    private fun calculateAccelerationMagnitude(values: FloatArray): Float {
        val x = values[0]
        val y = values[1]
        val z = values[2]
        return kotlin.math.sqrt(x * x + y * y + z * z)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        barometer=sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        accelerometData=TimeSeries(TIME_SERIES_SIZE)
        barometerData=TimeSeries(TIME_SERIES_SIZE)
        // Register sensor listeners
        sensorManager.registerListener(accelerometerListener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(barometerListener,barometer,SensorManager.SENSOR_DELAY_NORMAL)
        checkMovement()
    }
    override fun onResume() {
        super.onResume()
        // Register sensor listeners

        sensorManager.registerListener(accelerometerListener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(barometerListener,barometer,SensorManager.SENSOR_DELAY_NORMAL)
        ON_PAUSE_DEACTIVATE=false
    }
    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners to save battery
        ON_PAUSE_DEACTIVATE=true
        sensorManager.unregisterListener(barometerListener)
        sensorManager.unregisterListener(accelerometerListener)

    }
}