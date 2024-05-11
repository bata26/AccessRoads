package it.unipi.accessroads

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import it.unipi.accessroads.model.AccessibilityPoint

class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    @SuppressLint("SetTextI18n")
    override fun getInfoContents(marker: Marker): View? {
        // 1. Get tag
        val point = marker.tag as? AccessibilityPoint ?: return null

        // 2. Inflate view and set title, address and rating
        val view = LayoutInflater.from(context).inflate(R.layout.marker_info_contents, null)
        view.findViewById<TextView>(R.id.text_view_type).text = point.type
        view.findViewById<TextView>(R.id.text_view_position).text = "(${point.latLng.latitude} , ${point.latLng.longitude})"
        view.findViewById<TextView>(R.id.text_view_rating).text = "Reliability: ${getReliability(point.counter)}"

        return view
    }
    private fun getReliability(counter: Int): String {
        if(counter <= 7)
            return "Low"
        else if (counter in 8..10)
            return "Medium"
        else
            return "High"
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the default window (white bubble) should be used
        return null
    }
}


