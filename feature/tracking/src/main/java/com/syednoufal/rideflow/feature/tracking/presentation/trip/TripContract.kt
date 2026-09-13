package com.syednoufal.rideflow.feature.tracking.presentation.trip

import com.syednoufal.rideflow.core.common.mvi.UiEffect
import com.syednoufal.rideflow.core.common.mvi.UiIntent
import com.syednoufal.rideflow.core.common.mvi.UiState
import com.syednoufal.rideflow.core.domain.model.Driver
import com.syednoufal.rideflow.core.domain.model.GeoPoint

/** The live trip-tracking screen's rendered state. */
data class TripUiState(
    val tripId: String = "",
    val pickup: GeoPoint? = null,
    val dropoff: GeoPoint? = null,
    val driver: Driver? = null,
    val statusHeadline: String = "Loading your trip...",
    val etaMinutes: Int? = null,
    val progressFraction: Float = 0f,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false,
    val finalFareTotal: Double? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface TripIntent : UiIntent {
    data object ErrorDismissed : TripIntent
}

sealed interface TripEffect : UiEffect {
    data class NavigateToPayment(val tripId: String, val amount: Double) : TripEffect
}
