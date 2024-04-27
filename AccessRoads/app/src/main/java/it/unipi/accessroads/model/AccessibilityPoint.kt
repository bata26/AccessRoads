package it.unipi.accessroads.model

import java.sql.Timestamp

data class PointLists(
    var PointList: ArrayList<AccessibilityPoint> = arrayListOf()
)

data class AccessibilityPoint(
    var id: String = "",
    var position: Position? = Position(0.0,0.0),
    var counter: Int = 0,
    var timestamp: Timestamp = Timestamp(0),
    var type: String = ""
)

data class Position(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)