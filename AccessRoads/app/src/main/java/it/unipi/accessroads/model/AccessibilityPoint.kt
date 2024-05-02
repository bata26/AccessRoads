package it.unipi.accessroads.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp

data class AccessibilityPoint(
    var id: String = "",
    var position: LatLng = LatLng(0.0 , 0.0),
    var counter: Int = 0,
    var timestamp: Timestamp = Timestamp(0,0),
    var type: String = ""
)
