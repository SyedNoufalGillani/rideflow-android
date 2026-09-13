package com.syednoufal.rideflow.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val RideFlowLightColorScheme = lightColorScheme(
    primary = RideFlowIndigo40,
    onPrimary = RideFlowIndigo99,
    primaryContainer = RideFlowIndigo90,
    onPrimaryContainer = RideFlowIndigo10,
    secondary = RideFlowTeal40,
    onSecondary = RideFlowNeutral99,
    secondaryContainer = RideFlowTeal90,
    onSecondaryContainer = RideFlowIndigo10,
    tertiary = RideFlowAmber50,
    onTertiary = RideFlowNeutral99,
    tertiaryContainer = RideFlowAmber90,
    onTertiaryContainer = RideFlowAmber30,
    error = RideFlowRed40,
    onError = RideFlowNeutral99,
    errorContainer = RideFlowRed90,
    onErrorContainer = RideFlowRed10,
    background = RideFlowNeutral99,
    onBackground = RideFlowNeutral10,
    surface = RideFlowNeutral99,
    onSurface = RideFlowNeutral10,
    surfaceVariant = RideFlowNeutralVariant90,
    onSurfaceVariant = RideFlowNeutralVariant30,
    outline = RideFlowNeutralVariant50,
)

private val RideFlowDarkColorScheme = darkColorScheme(
    primary = RideFlowIndigo80,
    onPrimary = RideFlowIndigo20,
    primaryContainer = RideFlowIndigo30,
    onPrimaryContainer = RideFlowIndigo90,
    secondary = RideFlowTeal80,
    onSecondary = RideFlowIndigo20,
    secondaryContainer = RideFlowTeal40,
    onSecondaryContainer = RideFlowTeal90,
    tertiary = RideFlowAmber80,
    onTertiary = RideFlowAmber30,
    tertiaryContainer = RideFlowAmber40,
    onTertiaryContainer = RideFlowAmber90,
    error = RideFlowRed80,
    onError = RideFlowRed10,
    errorContainer = RideFlowRed40,
    onErrorContainer = RideFlowRed90,
    background = RideFlowNeutral10,
    onBackground = RideFlowNeutral90,
    surface = RideFlowNeutral10,
    onSurface = RideFlowNeutral90,
    surfaceVariant = RideFlowNeutralVariant30,
    onSurfaceVariant = RideFlowNeutralVariant80,
    outline = RideFlowNeutralVariant50,
)

val LocalRideFlowSpacingProvider = staticCompositionLocalOf { RideFlowSpacing() }
val LocalRideFlowExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/**
 * RideFlow's root theme composable. Applies the light/dark Material3 color
 * scheme, typography and shape tokens, and exposes [RideFlowSpacing] /
 * [RideFlowExtendedColors] through composition locals consumed via
 * [RideFlowTheme] object accessors ([RideFlowTheme.spacing], etc.).
 *
 * @param dynamicColor when `true` (the default), prefers Android 12+'s
 * wallpaper-derived dynamic color over the static brand palette.
 */
@Composable
fun RideFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> RideFlowDarkColorScheme
        else -> RideFlowLightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalRideFlowSpacingProvider provides RideFlowSpacing(),
        LocalRideFlowExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RideFlowTypography,
            shapes = RideFlowShapes,
            content = content,
        )
    }
}

/** Convenience accessors mirroring `MaterialTheme.colorScheme` / `.typography` for RideFlow's own tokens. */
object RideFlowTheme {
    val spacing: RideFlowSpacing
        @Composable
        get() = LocalRideFlowSpacingProvider.current

    val extendedColors: RideFlowExtendedColors
        @Composable
        get() = LocalRideFlowExtendedColors.current
}
