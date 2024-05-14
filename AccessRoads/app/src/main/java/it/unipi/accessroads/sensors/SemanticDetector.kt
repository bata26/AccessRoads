package it.unipi.accessroads.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import it.unipi.accessroads.utils.FilterType
import it.unipi.accessroads.utils.TimeSeries
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
interface SemanticDetectionListener {
    fun onSemanticEventDetected(semanticInfo: SemanticType)
}
enum class SemanticType{
    NONE,ELEVATOR,ROUGH_ROAD
}
class SemanticDetector(private val context: Context) {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var barometer: Sensor? = null
    private var detectionListener: SemanticDetectionListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private val TIME_SERIES_SIZE = 100
    private val WINDOW_SIZE = 24 // Must be an even number
    private lateinit var accelerometerData: TimeSeries
    private lateinit var barometerData: TimeSeries
    private var detected = false
    private val ELEVATOR_BAR_CHANGE = 0.20f
    private val ELEVATOR_ACC_CHANGE = 0.12f
    private val ROUGH_ROAD_STDDEV = 1.0f
    private val runnableDetector=object : Runnable {
        override fun run() {
            var semantic:SemanticType=SemanticType.NONE
            val valuesBar = barometerData.getWindowToAnalyze(WINDOW_SIZE, FilterType.LOW_PASS)
            val valuesAcc = accelerometerData.getWindowToAnalyze(WINDOW_SIZE, FilterType.LOW_PASS)

            if (valuesBar[0] > 0 && abs(valuesBar[0] - valuesBar[WINDOW_SIZE - 1]) >= ELEVATOR_BAR_CHANGE && abs(valuesBar[0] - valuesBar[WINDOW_SIZE - 1]) < 5) {
                val halfWindow = WINDOW_SIZE / 2
                val firstHalfWindow = halfWindow - (halfWindow / 2)
                val secondHalfWindow = halfWindow + (halfWindow / 2)

                if (abs(valuesAcc[0] - valuesAcc[firstHalfWindow]) >= ELEVATOR_ACC_CHANGE || abs(valuesAcc[WINDOW_SIZE - 1] - valuesAcc[secondHalfWindow]) >= ELEVATOR_ACC_CHANGE) {
                    Toast.makeText(context, "Elevator detected", Toast.LENGTH_SHORT).show()
                    semantic=SemanticType.ELEVATOR
                    detected = true
                }
            }

            if (valuesBar[0] > 0 && abs(valuesBar[0] - valuesBar[WINDOW_SIZE - 1]) < ELEVATOR_BAR_CHANGE) {
                if (calculateStandardDeviation(valuesAcc) >= ROUGH_ROAD_STDDEV) {
                    Toast.makeText(context, "Rough Road Detected with std->"+calculateStandardDeviation(valuesAcc), Toast.LENGTH_SHORT).show()
                    detected = true
                    semantic=SemanticType.ROUGH_ROAD
                }
            }

            if (detected) {
                detected = false
                handler.removeCallbacks(this)
                handler.postDelayed(this, 8000L)
                detectionListener?.onSemanticEventDetected(semantic)
                Toast.makeText(context, "Sleep 8 secs", Toast.LENGTH_SHORT).show()
            } else {
                handler.postDelayed(this, 4000L)
            }
        }
    }
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
            accelerometerData.addValue(accelerationMagnitude)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }
    fun setSemanticDetectionListener(listener: SemanticDetectionListener) {
        this.detectionListener = listener
    }
    private fun checkMovement() {
        handler.post(runnableDetector)
    }

    private fun calculateStandardDeviation(values: FloatArray): Float {
        val mean = values.average()
        val sumOfSquaredDifferences = values.sumOf { (it - mean).pow(2) }
        val variance = sumOfSquaredDifferences / values.size
        return sqrt(variance).toFloat()
    }

    private fun calculateAccelerationMagnitude(values: FloatArray): Float {
        val x = values[0]
        val y = values[1]
        val z = values[2]
        return sqrt(x * x + y * y + z * z)
    }

    fun startDetection() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        accelerometerData = TimeSeries(TIME_SERIES_SIZE)
        barometerData = TimeSeries(TIME_SERIES_SIZE)

        // Register sensor listeners
        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(barometerListener, barometer, SensorManager.SENSOR_DELAY_NORMAL)
        checkMovement()
    }

    fun stopDetection() {
        sensorManager.unregisterListener(barometerListener)
        sensorManager.unregisterListener(accelerometerListener)
        handler.removeCallbacks(runnableDetector)
    }
}
