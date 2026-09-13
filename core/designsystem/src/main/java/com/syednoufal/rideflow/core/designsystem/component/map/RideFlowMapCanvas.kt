package com.syednoufal.rideflow.core.designsystem.component.map

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/** A point on the map's route, normalized to `0f..1f` on both axes, with a heading for oriented markers. */
data class RoutePoint(val position: Offset, val headingDegrees: Float)

/**
 * Computes positions along a stylized, road-like route between two normalized
 * points, bending once (horizontal, then vertical) rather than cutting a
 * straight diagonal line — approximating how a real routing engine snaps a
 * path to a street grid, without needing an actual map or geocoding.
 */
object RideFlowRoute {

    /** The route's single bend point, where it turns from horizontal to vertical travel. */
    fun bendPoint(start: Offset, end: Offset): Offset = Offset(end.x, start.y)

    /** The full polyline (start → bend → end) used both for drawing and for [pointAt] calculations. */
    fun polyline(start: Offset, end: Offset): List<Offset> = listOf(start, bendPoint(start, end), end)

    /** The point and heading at [progress] (`0f` = [start], `1f` = [end]) along the bent route. */
    fun pointAt(start: Offset, end: Offset, progress: Float): RoutePoint {
        val clamped = progress.coerceIn(0f, 1f)
        val bend = bendPoint(start, end)
        val horizontalLength = hypot((bend.x - start.x).toDouble(), (bend.y - start.y).toDouble()).toFloat()
        val verticalLength = hypot((end.x - bend.x).toDouble(), (end.y - bend.y).toDouble()).toFloat()
        val totalLength = horizontalLength + verticalLength

        if (totalLength <= 0f) return RoutePoint(start, headingDegrees = 0f)

        val distance = clamped * totalLength
        return if (distance <= horizontalLength) {
            val segmentProgress = if (horizontalLength == 0f) 0f else distance / horizontalLength
            RoutePoint(
                position = lerp(start, bend, segmentProgress),
                headingDegrees = headingBetween(start, bend),
            )
        } else {
            val segmentProgress = if (verticalLength == 0f) 0f else (distance - horizontalLength) / verticalLength
            RoutePoint(
                position = lerp(bend, end, segmentProgress),
                headingDegrees = headingBetween(bend, end),
            )
        }
    }

    private fun lerp(start: Offset, end: Offset, fraction: Float): Offset = Offset(
        x = start.x + (end.x - start.x) * fraction,
        y = start.y + (end.y - start.y) * fraction,
    )

    private fun headingBetween(from: Offset, to: Offset): Float {
        val degrees = Math.toDegrees(atan2((to.y - from.y).toDouble(), (to.x - from.x).toDouble()))
        return degrees.toFloat()
    }
}

/**
 * A stylized, fully custom Canvas-drawn map: a procedural road grid, pickup
 * and dropoff pins connected by a road-snapped route, and an optional
 * oriented driver marker. Requires no Google Maps API key and no network
 * access, so the booking and tracking flows are interactive out of the box.
 *
 * All positions are normalized to the unit square (`0f..1f` on both axes);
 * callers project real coordinates (or simulated ones) onto that square.
 */
@Composable
fun RideFlowMapCanvas(
    pickup: Offset,
    dropoff: Offset,
    modifier: Modifier = Modifier,
    driverPosition: Offset? = null,
    driverHeadingDegrees: Float = 0f,
    showSearchingPulse: Boolean = false,
) {
    val extendedColors = RideFlowTheme.extendedColors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface

    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "map_pulse_value",
    )

    // Fixed, hand-tuned fractional grid lines so the "city block" pattern
    // looks intentional and stays stable across recompositions.
    val verticalRoads = remember { listOf(0.12f, 0.3f, 0.5f, 0.68f, 0.85f) }
    val horizontalRoads = remember { listOf(0.18f, 0.38f, 0.58f, 0.78f) }
    val majorRoads = remember { setOf(0.3f, 0.58f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.mapBackground),
    ) {
        val width = size.width
        val height = size.height

        // --- Procedural road grid ---
        verticalRoads.forEach { fraction ->
            val x = fraction * width
            drawLine(
                color = if (fraction in majorRoads) extendedColors.mapRoad else extendedColors.mapRoadMinor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = if (fraction in majorRoads) 5.dp.toPx() else 2.dp.toPx(),
            )
        }
        horizontalRoads.forEach { fraction ->
            val y = fraction * height
            drawLine(
                color = if (fraction in majorRoads) extendedColors.mapRoad else extendedColors.mapRoadMinor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = if (fraction in majorRoads) 5.dp.toPx() else 2.dp.toPx(),
            )
        }

        val pickupPx = Offset(pickup.x * width, pickup.y * height)
        val dropoffPx = Offset(dropoff.x * width, dropoff.y * height)
        val polylinePx = RideFlowRoute.polyline(pickup, dropoff).map { Offset(it.x * width, it.y * height) }

        // --- Route line, dashed to read as a suggested path rather than a road itself ---
        val routePath = Path().apply {
            moveTo(polylinePx.first().x, polylinePx.first().y)
            polylinePx.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = routePath,
            color = primaryColor,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)),
            ),
        )

        // --- Searching-for-driver radar pulse, centered on pickup ---
        if (showSearchingPulse) {
            val maxRadius = 60.dp.toPx()
            drawCircle(
                color = primaryColor.copy(alpha = (1f - pulseProgress) * 0.35f),
                radius = maxRadius * pulseProgress,
                center = pickupPx,
            )
        }

        // --- Pickup pin ---
        drawCircle(color = primaryColor.copy(alpha = 0.18f), radius = 16.dp.toPx(), center = pickupPx)
        drawCircle(color = primaryColor, radius = 8.dp.toPx(), center = pickupPx)
        drawCircle(color = surfaceColor, radius = 3.dp.toPx(), center = pickupPx)

        // --- Dropoff pin (teardrop-style pin shape) ---
        drawPinShape(center = dropoffPx, color = secondaryColor, surfaceColor = surfaceColor)

        // --- Driver marker ---
        if (driverPosition != null) {
            val driverPx = Offset(driverPosition.x * width, driverPosition.y * height)
            rotate(degrees = driverHeadingDegrees, pivot = driverPx) {
                drawCarMarker(center = driverPx, color = primaryColor, surfaceColor = surfaceColor)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPinShape(
    center: Offset,
    color: Color,
    surfaceColor: Color,
) {
    val pinHeight = 26.dp.toPx()
    val pinRadius = 9.dp.toPx()
    val tip = Offset(center.x, center.y + pinHeight * 0.4f)
    val bulbCenter = Offset(center.x, center.y - pinHeight * 0.25f)

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(bulbCenter.x - pinRadius * 0.9f, bulbCenter.y + pinRadius * 0.4f)
        lineTo(bulbCenter.x + pinRadius * 0.9f, bulbCenter.y + pinRadius * 0.4f)
        close()
    }
    drawPath(path = path, color = color)
    drawCircle(color = color, radius = pinRadius, center = bulbCenter)
    drawCircle(color = surfaceColor, radius = pinRadius * 0.4f, center = bulbCenter)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCarMarker(
    center: Offset,
    color: Color,
    surfaceColor: Color,
) {
    val bodyWidth = 20.dp.toPx()
    val bodyHeight = 11.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - bodyWidth / 2, center.y - bodyHeight / 2),
        size = androidx.compose.ui.geometry.Size(bodyWidth, bodyHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyHeight / 2, bodyHeight / 2),
    )
    // A small notch toward the "front" of the car so its heading reads clearly once rotated.
    drawCircle(
        color = surfaceColor,
        radius = bodyHeight * 0.22f,
        center = Offset(center.x + bodyWidth / 2 - bodyHeight * 0.35f, center.y),
    )
}

/** Rounds a normalized progress to whole percent, useful for accessibility/debug labels. */
fun Float.toPercentInt(): Int = (this.coerceIn(0f, 1f) * 100f).roundToInt()
