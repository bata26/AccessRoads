package it.unipi.accessroads.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import it.unipi.accessroads.R
import it.unipi.accessroads.model.AccessibilityPoint

class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    @SuppressLint("SetTextI18n", "InflateParams")
    override fun getInfoContents(marker: Marker): View? {
        // 1. Get tag
        val point = marker.tag as? AccessibilityPoint ?: return null

        // 2. Inflate view and set title, address and rating
        val view = LayoutInflater.from(context).inflate(R.layout.marker_info_contents, null)
        view.findViewById<TextView>(R.id.text_view_type).text = point.type
        view.findViewById<TextView>(R.id.text_view_rating).text = "Reliability: ${getReliability(point.counter)}"

        return view
    }
    private fun getReliability(counter: Int): String {
        return when {
            counter <= 7 -> "Low"
            counter in 8..10 -> "Medium"
            else -> "High"
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the default window (white bubble) should be used
        return null
    }
}


