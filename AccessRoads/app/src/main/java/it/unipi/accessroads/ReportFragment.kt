package it.unipi.accessroads

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import it.unipi.accessroads.databinding.FragmentReportBinding
import it.unipi.accessroads.model.AccessibilityPoint
import it.unipi.accessroads.sensors.*
import it.unipi.accessroads.utils.Db
import it.unipi.accessroads.R as K


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ReportFragment : Fragment() {
    private lateinit var gpsManager: GPSManager
    private var _binding: FragmentReportBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gpsManager = GPSManager(requireContext())
        val spinner: Spinner = binding.reportSpinner
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            view.context,
            K.array.report_array,
            R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spinner.adapter = adapter
        }
        binding.reportButton.setOnClickListener {
            val type = spinner.getSelectedItem().toString()
            sendReport(type)
            val snack = Snackbar.make(it, "$type submitted!", Snackbar.LENGTH_LONG)
            snack.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun sendReport(type : String){
        gpsManager.getLastKnownLocation(object :LocationResultListener{
            override fun onLocationResult(location: MutableMap<String, Double>) {
                val latitude=location["lat"]
                val longitude=location["long"]
                if(latitude!=null&&longitude!=null){
                    val latLng= LatLng(latitude, longitude)
                    send(latLng, type)
                }else{
                    Log.d("error","Location is null")
                }
            }
        })
    }
    private fun send(latLng: LatLng, type : String){
        val accessibilityPoint= AccessibilityPoint(Db.getUUID(),latLng,1,type)
        Db.postPoint(accessibilityPoint)
    }

}