package com.syednoufal.rideflow.core.designsystem.component.map

import androidx.compose.ui.geometry.Offset
import com.syednoufal.rideflow.core.domain.model.GeoPoint

/**
 * Projects a real (latitude, longitude) coordinate onto the unit square
 * consumed by [RideFlowMapCanvas], within a fixed demo bounding box roughly
 * covering the San Francisco Bay Area — where every seeded saved place and
 * simulated driver position in this project falls. A production app would
 * replace this with an actual map SDK's projection; here it lets the same
 * stylized Canvas map represent real-looking coordinates consistently across
 * the booking and tracking screens with no map SDK involved.
 */
object DemoGeoProjection {

    private const val MIN_LATITUDE = 37.35
    private const val MAX_LATITUDE = 37.85
    private const val MIN_LONGITUDE = -122.55
    private const val MAX_LONGITUDE = -122.10
    private const val EDGE_MARGIN = 0.06f

    fun project(point: GeoPoint): Offset {
        val xFraction = (point.longitude - MIN_LONGITUDE) / (MAX_LONGITUDE - MIN_LONGITUDE)
        val yFraction = 1.0 - (point.latitude - MIN_LATITUDE) / (MAX_LATITUDE - MIN_LATITUDE)
        return Offset(
            x = xFraction.toFloat().coerceIn(EDGE_MARGIN, 1f - EDGE_MARGIN),
            y = yFraction.toFloat().coerceIn(EDGE_MARGIN, 1f - EDGE_MARGIN),
        )
    }
}
