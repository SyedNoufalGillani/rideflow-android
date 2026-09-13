package com.syednoufal.rideflow.feature.booking.data.remote

import com.syednoufal.rideflow.core.network.RideFlowApiService
import com.syednoufal.rideflow.core.network.dto.FareEstimateRequestDto
import com.syednoufal.rideflow.core.network.dto.GeoPointDto
import com.syednoufal.rideflow.core.network.dto.RideRequestDto
import javax.inject.Inject

/** Thin wrapper around [RideFlowApiService] for the booking endpoints. */
class BookingRemoteDataSource @Inject constructor(
    private val apiService: RideFlowApiService,
) {
    suspend fun getRideTiers() = apiService.getRideTiers()

    suspend fun getFareEstimates(pickup: GeoPointDto, dropoff: GeoPointDto) =
        apiService.getFareEstimates(FareEstimateRequestDto(pickup, dropoff))

    suspend fun requestRide(pickup: GeoPointDto, dropoff: GeoPointDto, rideTierType: String) =
        apiService.requestRide(RideRequestDto(pickup, dropoff, rideTierType))

    suspend fun getTrip(tripId: String) = apiService.getTrip(tripId)

    suspend fun cancelRide(tripId: String) = apiService.cancelRide(tripId)
}
