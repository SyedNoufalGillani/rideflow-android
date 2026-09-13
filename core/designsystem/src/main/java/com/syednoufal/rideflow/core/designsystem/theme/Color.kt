package com.syednoufal.rideflow.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Brand — a deep indigo/violet paired with a warm amber accent, chosen to
// read as distinct from the black-and-green palette common in this space.
internal val RideFlowIndigo10 = Color(0xFF0B0A2A)
internal val RideFlowIndigo20 = Color(0xFF171555)
internal val RideFlowIndigo30 = Color(0xFF272292)
internal val RideFlowIndigo40 = Color(0xFF3730C4)
internal val RideFlowIndigo50 = Color(0xFF4F46E5)
internal val RideFlowIndigo60 = Color(0xFF6D64EE)
internal val RideFlowIndigo80 = Color(0xFFB8B2F7)
internal val RideFlowIndigo90 = Color(0xFFE1DEFB)
internal val RideFlowIndigo95 = Color(0xFFF0EEFD)
internal val RideFlowIndigo99 = Color(0xFFFCFBFF)

internal val RideFlowAmber30 = Color(0xFF7A4A00)
internal val RideFlowAmber40 = Color(0xFFA15E00)
internal val RideFlowAmber50 = Color(0xFFD97E00)
internal val RideFlowAmber60 = Color(0xFFF59E0B)
internal val RideFlowAmber80 = Color(0xFFFFCF87)
internal val RideFlowAmber90 = Color(0xFFFFE3B8)

internal val RideFlowTeal40 = Color(0xFF00796B)
internal val RideFlowTeal50 = Color(0xFF00967F)
internal val RideFlowTeal60 = Color(0xFF13B79E)
internal val RideFlowTeal80 = Color(0xFF8CE8D6)
internal val RideFlowTeal90 = Color(0xFFC5F5EA)

internal val RideFlowRed40 = Color(0xFFBA1A1A)
internal val RideFlowRed80 = Color(0xFFFFB4AB)
internal val RideFlowRed90 = Color(0xFFFFDAD6)
internal val RideFlowRed10 = Color(0xFF410002)

internal val RideFlowNeutral10 = Color(0xFF1B1B1F)
internal val RideFlowNeutral20 = Color(0xFF303034)
internal val RideFlowNeutral90 = Color(0xFFE4E1E9)
internal val RideFlowNeutral95 = Color(0xFFF3EFF7)
internal val RideFlowNeutral99 = Color(0xFFFDFBFF)

internal val RideFlowNeutralVariant30 = Color(0xFF454650)
internal val RideFlowNeutralVariant50 = Color(0xFF767680)
internal val RideFlowNeutralVariant80 = Color(0xFFC6C5D0)
internal val RideFlowNeutralVariant90 = Color(0xFFE3E1EC)

/**
 * Colors with clear ride-hailing semantics (surge pricing, driver-en-route,
 * trip completed) that don't map to a standard Material3 role. Exposed via
 * [com.syednoufal.rideflow.core.designsystem.theme.LocalRideFlowExtendedColors].
 */
data class RideFlowExtendedColors(
    val surge: Color,
    val onSurge: Color,
    val success: Color,
    val onSuccess: Color,
    val mapRoad: Color,
    val mapRoadMinor: Color,
    val mapBackground: Color,
)

internal val LightExtendedColors = RideFlowExtendedColors(
    surge = RideFlowAmber60,
    onSurge = Color.White,
    success = RideFlowTeal50,
    onSuccess = Color.White,
    mapRoad = Color(0xFFD8D6E3),
    mapRoadMinor = Color(0xFFE9E7F2),
    mapBackground = Color(0xFFF3F1FA),
)

internal val DarkExtendedColors = RideFlowExtendedColors(
    surge = RideFlowAmber80,
    onSurge = RideFlowAmber30,
    success = RideFlowTeal80,
    onSuccess = RideFlowTeal40,
    mapRoad = Color(0xFF38394A),
    mapRoadMinor = Color(0xFF26273A),
    mapBackground = Color(0xFF16172A),
)
