package it.unipi.accessroads.utils

import android.content.ContentValues.TAG
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.unipi.accessroads.model.AccessibilityPoint
import java.util.UUID
import kotlin.math.*

class Db {
    companion object {
        private const val DIST_BETWEEN_2_POINTS_MAX:Double=0.02

        fun getPoints(callback: (List<AccessibilityPoint>) -> Unit ) {
            Log.d(TAG , "PRE READ")
            val db = Firebase.firestore
            val accessibilityPoints = mutableListOf<AccessibilityPoint>()
            db.collection("points")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        val geoPoint = document.getGeoPoint("position")
                        val pos = geoPoint?.let { LatLng(geoPoint.latitude, geoPoint.longitude) }
                        Log.d("puntipos","${geoPoint?.latitude},${geoPoint?.longitude},${data["type"]},${data["counter"]}")
                        val accessibilityPoint = AccessibilityPoint(
                            id = document.id,
                            latLng = pos ?: LatLng(0.0, 0.0), // Default value if position is null
                            counter = (data["counter"] as Long).toInt(),
                            type = data["type"] as String
                        )
                        accessibilityPoints.add(accessibilityPoint)
                    }
                    callback(accessibilityPoints)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                    callback(emptyList())
                }
        }

        fun getUUID(): String {
            return UUID.randomUUID().toString()
        }

        fun postPoint(accessibilityPoint: AccessibilityPoint) {
            val db = Firebase.firestore

            // Query points with the same semantic type
            val query = db.collection("points")
                .whereEqualTo("type", accessibilityPoint.type)

            query.get().addOnSuccessListener { querySnapshot ->
                val newPointLocation = accessibilityPoint.latLng
                // Iterate through the query results
                for (document in querySnapshot) {
                    val data = document.data
                    val geoPoint = document.getGeoPoint("position")
                    val pos = geoPoint?.let { LatLng(geoPoint.latitude, geoPoint.longitude) }

                    val accessibilityPointTest = AccessibilityPoint(
                        id = document.id,
                        latLng = pos ?: LatLng(0.0, 0.0), // Default value if position is null
                        counter = (data["counter"] as Long).toInt(),
                        type = data["type"] as String
                    )
                    Log.d("Query", "Document: ${accessibilityPointTest.latLng.latitude},${accessibilityPointTest.latLng.longitude}")
                    val existingPointLocation = accessibilityPointTest.latLng

                    // Calculate distance between new point and existing point
                    val distance = calculateDistance(newPointLocation.latitude,newPointLocation.longitude, existingPointLocation.latitude,existingPointLocation.longitude)
                    println(distance)
                    if (distance <= DIST_BETWEEN_2_POINTS_MAX) {
                        val newCounter = accessibilityPointTest.counter + 1
                        db.collection("points").document(document.id)
                            .update("counter", newCounter)
                            .addOnSuccessListener {
                                Log.d("Firebase Update", "Counter updated for existing point")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase Update", "Error updating counter: $e")
                            }
                        return@addOnSuccessListener
                    }
                }

                val point = hashMapOf(
                    "_id" to accessibilityPoint.id,
                    "position" to GeoPoint(accessibilityPoint.latLng.latitude, accessibilityPoint.latLng.longitude),
                    "counter" to accessibilityPoint.counter,
                    "type" to accessibilityPoint.type
                )

                db.collection("points").document(accessibilityPoint.id)
                    .set(point)
                    .addOnSuccessListener {
                        Log.d("Firebase Insert", "New point inserted!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase Insert", "Error writing document: $e")
                    }
            }.addOnFailureListener { e ->
                Log.e("Firebase Query", "Error querying documents: $e")
            }
        }
        private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadius = 6371 // Earth's radius in kilometers
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadius * c
        }
    }
}

