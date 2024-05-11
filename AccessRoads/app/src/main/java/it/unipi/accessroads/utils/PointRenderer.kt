package it.unipi.accessroads.utils

import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import it.unipi.accessroads.BitmapHelper
import it.unipi.accessroads.R
import it.unipi.accessroads.model.AccessibilityPoint

/**
 * A custom cluster renderer for Accessibility objects.
 */
class PointRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<AccessibilityPoint>
) : DefaultClusterRenderer<AccessibilityPoint>(context, map, clusterManager) {

    /**
     * The icons to use for each cluster item
     */

    private val elevatorIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(context, R.drawable.ic_elevator_24, Color.parseColor("#2AAC17"))
    }

    private val roughRodeIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(context, R.drawable.ic_road_24, Color.parseColor("#B70202"))
    }

    private val stairsIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(context, R.drawable.ic_stairs_24, Color.parseColor("#545454"))
    }

    /**
     * Method called before the cluster item (i.e. the marker) is rendered. This is where marker
     * options should be set
     */
    override fun onBeforeClusterItemRendered(item: AccessibilityPoint, markerOptions: MarkerOptions) {
        val icon : BitmapDescriptor
        when (item.type) {
            "Elevator" -> {
                icon = elevatorIcon
            }
            "Rough Road" -> {
                icon = roughRodeIcon
            }
            "Stairs" -> {
                icon = stairsIcon
            }
            else -> {
                icon = BitmapDescriptorFactory.defaultMarker()
            }
        }

        markerOptions.title(item.type)
            .position(item.getPosition())
            .icon(icon)
    }

    /**
     * Method called right after the cluster item (i.e. the marker) is rendered. This is where
     * properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: AccessibilityPoint, marker: Marker) {
        marker.tag = clusterItem
    }
}