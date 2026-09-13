package com.syednoufal.rideflow.feature.booking.data.repository

import com.syednoufal.rideflow.core.common.dispatcher.DispatcherProvider
import com.syednoufal.rideflow.core.database.dao.SavedPlaceDao
import com.syednoufal.rideflow.core.domain.model.DomainException
import com.syednoufal.rideflow.core.domain.model.Driver
import com.syednoufal.rideflow.core.domain.model.FareEstimate
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.RideTier
import com.syednoufal.rideflow.core.domain.model.RideTierType
import com.syednoufal.rideflow.core.domain.model.Trip
import com.syednoufal.rideflow.core.domain.model.TripStatus
import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import com.syednoufal.rideflow.core.network.dto.DriverDto
import com.syednoufal.rideflow.core.network.dto.FareEstimateDto
import com.syednoufal.rideflow.core.network.dto.GeoPointDto
import com.syednoufal.rideflow.core.network.dto.RideTierDto
import com.syednoufal.rideflow.core.network.dto.TripDto
import com.syednoufal.rideflow.feature.booking.data.remote.BookingRemoteDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A real, Retrofit-backed [BookingRepository] implementation. Not selected
 * by default — see `BuildConfig.USE_FAKE_DATA_SOURCE` and
 * [FakeBookingRepository] — but demonstrates how this repository polls a
 * real dispatch backend for driver-matching updates.
 */
@Singleton
class RealBookingRepository @Inject constructor(
    private val remoteDataSource: BookingRemoteDataSource,
    private val savedPlaceDao: SavedPlaceDao,
    private val dispatcherProvider: DispatcherProvider,
) : BookingRepository {

    override suspend fun getRideTiers(): List<RideTier> = withContext(dispatcherProvider.io) {
        remoteDataSource.getRideTiers().map { it.toDomain() }
    }

    override suspend fun getSavedPlaces(): List<GeoPoint> = withContext(dispatcherProvider.io) {
        savedPlaceDao.getAll().map {
            GeoPoint(latitude = it.latitude, longitude = it.longitude, label = it.label, address = it.address)
        }
    }

    override suspend fun getFareEstimates(pickup: GeoPoint, dropoff: GeoPoint): List<FareEstimate> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.getFareEstimates(pickup.toDto(), dropoff.toDto()).map { it.toDomain() }
        }

    override suspend fun requestRide(
        pickup: GeoPoint,
        dropoff: GeoPoint,
        rideTierType: RideTierType,
        fareEstimate: FareEstimate,
    ): Trip = withContext(dispatcherProvider.io) {
        remoteDataSource.requestRide(pickup.toDto(), dropoff.toDto(), rideTierType.name).toDomain()
    }

    override fun observeDriverMatching(tripId: String): Flow<Trip> = flow {
        while (true) {
            val tripDto = remoteDataSource.getTrip(tripId)
            val trip = tripDto.toDomain()
            emit(trip)
            if (trip.status is TripStatus.DriverArrived || trip.status is TripStatus.Cancelled) break
            delay(POLL_INTERVAL_MILLIS)
        }
    }.flowOn(dispatcherProvider.io)

    override suspend fun cancelRide(tripId: String) = withContext(dispatcherProvider.io) {
        remoteDataSource.cancelRide(tripId)
    }

    private fun RideTierDto.toDomain() = RideTier(
        type = RideTierType.valueOf(type),
        displayName = displayName,
        description = description,
        capacity = capacity,
        baseFareMultiplier = baseFareMultiplier,
        etaMinutes = etaMinutes,
    )

    private fun FareEstimateDto.toDomain() = FareEstimate(
        rideTierType = RideTierType.valueOf(rideTierType),
        currencyCode = currencyCode,
        baseFare = baseFare,
        distanceFare = distanceFare,
        timeFare = timeFare,
        surgeMultiplier = surgeMultiplier,
        estimatedDistanceKm = estimatedDistanceKm,
        estimatedDurationMinutes = estimatedDurationMinutes,
    )

    private fun GeoPoint.toDto() = GeoPointDto(latitude, longitude, label, address)

    private fun DriverDto.toDomain() = Driver(
        id = id,
        fullName = fullName,
        photoUrl = photoUrl,
        rating = rating,
        vehicleModel = vehicleModel,
        vehiclePlate = vehiclePlate,
        phoneNumber = phoneNumber,
        currentLocation = GeoPoint(currentLocation.latitude, currentLocation.longitude),
    )

    private fun TripDto.toDomain(): Trip {
        val rideTier = RideTierType.valueOf(rideTierType)
        val status = when (status) {
            "SEARCHING" -> TripStatus.SearchingForDriver
            "DRIVER_ASSIGNED" -> TripStatus.DriverAssigned(
                driver = requireNotNull(driver).toDomain(),
                etaMinutes = DEFAULT_ETA_MINUTES,
            )
            "DRIVER_ARRIVED" -> TripStatus.DriverArrived(driver = requireNotNull(driver).toDomain())
            "CANCELLED" -> TripStatus.Cancelled(reason = "Cancelled by dispatch")
            else -> throw DomainException.TripNotFoundException(id)
        }
        return Trip(
            id = id,
            pickup = GeoPoint(pickup.latitude, pickup.longitude, pickup.label, pickup.address),
            dropoff = GeoPoint(dropoff.latitude, dropoff.longitude, dropoff.label, dropoff.address),
            rideTierType = rideTier,
            fareEstimate = fareEstimate.toDomain(),
            status = status,
            requestedAtEpochMillis = requestedAtEpochMillis,
        )
    }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 3000L
        const val DEFAULT_ETA_MINUTES = 4
    }
}
