package com.syednoufal.rideflow.feature.tracking.presentation.trip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.syednoufal.rideflow.core.common.mvi.MviViewModel
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.TripStatus
import com.syednoufal.rideflow.core.domain.usecase.tracking.ObserveActiveTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives the live trip-tracking screen: driver location, ETA, and trip completion. */
@HiltViewModel
class TripViewModel @Inject constructor(
    observeActiveTripUseCase: ObserveActiveTripUseCase,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<TripUiState, TripIntent, TripEffect>(
    TripUiState(tripId = checkNotNull(savedStateHandle[ARG_TRIP_ID]) { "tripId is required" }),
) {

    init {
        viewModelScope.launch {
            runCatching {
                observeActiveTripUseCase(currentState.tripId).collect { trip ->
                    applyStatus(trip.status, pickup = trip.pickup, dropoff = trip.dropoff)
                }
            }.onFailure { throwable ->
                setState { copy(isLoading = false, errorMessage = throwable.message ?: "Couldn't load this trip.") }
            }
        }
    }

    override fun onIntent(intent: TripIntent) {
        when (intent) {
            is TripIntent.ErrorDismissed -> setState { copy(errorMessage = null) }
        }
    }

    private fun applyStatus(status: TripStatus, pickup: GeoPoint, dropoff: GeoPoint) {
        when (status) {
            is TripStatus.InProgress -> setState {
                copy(
                    isLoading = false,
                    pickup = pickup,
                    dropoff = dropoff,
                    driver = status.driver,
                    statusHeadline = "On the way to your destination",
                    etaMinutes = status.etaMinutes,
                    progressFraction = status.progressFraction,
                )
            }
            is TripStatus.Completed -> {
                setState {
                    copy(
                        isLoading = false,
                        pickup = pickup,
                        dropoff = dropoff,
                        driver = status.driver,
                        statusHeadline = "You've arrived!",
                        progressFraction = 1f,
                        isCompleted = true,
                        finalFareTotal = status.finalFare.total,
                    )
                }
                sendEffect { TripEffect.NavigateToPayment(currentState.tripId, status.finalFare.total) }
            }
            is TripStatus.Cancelled -> setState {
                copy(isLoading = false, errorMessage = "This trip was cancelled.")
            }
            else -> Unit
        }
    }

    companion object {
        const val ARG_TRIP_ID = "tripId"
    }
}
