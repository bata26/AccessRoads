package it.unipi.accessroads.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.clustering.ClusterItem

data class AccessibilityPoint(
    var id: String = "",
    var latLng: LatLng = LatLng(0.0 , 0.0),
    var counter: Int = 0,
    var timestamp: Timestamp = Timestamp(0,0),
    var type: String = ""
) : ClusterItem {
    override fun getPosition(): LatLng =
        latLng

    override fun getTitle(): String =
        type

    override fun getSnippet(): String =
        counter.toString()
}
