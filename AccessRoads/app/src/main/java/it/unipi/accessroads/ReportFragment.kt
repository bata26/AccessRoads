package it.unipi.accessroads

import GPSManager
import LocationResultListener
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import it.unipi.accessroads.databinding.FragmentReportBinding
import com.google.android.material.snackbar.Snackbar
import it.unipi.accessroads.model.AccessibilityPoint

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
        gpsManager=GPSManager(requireContext())
        binding.reportButton.setOnClickListener {
            sendReport()
            val snack = Snackbar.make(it,"Report submitted!",Snackbar.LENGTH_LONG)
            snack.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun sendReport(){
        gpsManager.getLastKnownLocation(object :LocationResultListener{
            override fun onLocationResult(location: MutableMap<String, Double>) {
                val latitude=location["lat"]
                val longitude=location["long"]
                if(latitude!=null&&longitude!=null){
                    val latLng= LatLng(latitude, longitude)
                    send(latLng)
                }else{
                    Log.d("error","Location is null")
                }
            }
        })
    }
    private fun send(latLng: LatLng){
        val accessibilityPoint=AccessibilityPoint(Db.getUUID(),latLng,1,"Stairs")
        Db.postPoint(accessibilityPoint)
    }

}