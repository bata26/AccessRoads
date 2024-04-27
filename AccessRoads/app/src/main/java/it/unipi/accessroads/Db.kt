package it.unipi.accessroads

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.unipi.accessroads.model.AccessibilityPoint
import it.unipi.accessroads.model.PointLists
import it.unipi.accessroads.model.Position
import java.util.UUID

class Db {
    companion object {

        fun getPoints() {
            Log.d(TAG , "PRE READ")
            val db = Firebase.firestore
            var pointRes: PointLists
            db.collection("points")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        val geoPoint = document.getGeoPoint("position")
                        val pos = geoPoint?.let { Position(geoPoint.latitude, it.longitude) }
                        val point = AccessibilityPoint(
                            document.id,
                            pos,
                            data.get("counter"),
                            data["timestamp"],
                            data["type"]
                         )
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }

        fun getUUID(): String {
            return UUID.randomUUID().toString()
        }

        fun postPoint(accessibilityPoint: AccessibilityPoint) {
            val db = Firebase.firestore
            val point = HashMap<String, Any>()
            point["_id"] = accessibilityPoint.id
            point["position"] = GeoPoint(accessibilityPoint.position.latitude, accessibilityPoint.position.longitude)
            point["counter"] = accessibilityPoint.counter
            point["timestamp"] = accessibilityPoint.timestamp
            point["type"] = accessibilityPoint.type
            db.collection("points").document(accessibilityPoint.id)
                .set(point)
                .addOnSuccessListener { println("DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> println("Error writing document: "+  e) }
        }
    }
}

