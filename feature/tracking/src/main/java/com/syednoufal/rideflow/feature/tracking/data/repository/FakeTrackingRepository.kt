package com.syednoufal.rideflow.feature.tracking.data.repository

import com.syednoufal.rideflow.core.common.dispatcher.DispatcherProvider
import com.syednoufal.rideflow.core.common.fake.DemoDriverCatalog
import com.syednoufal.rideflow.core.database.dao.RideHistoryDao
import com.syednoufal.rideflow.core.database.entity.RideHistoryEntity
import com.syednoufal.rideflow.core.domain.model.DomainException
import com.syednoufal.rideflow.core.domain.model.FareEstimate
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.RideTierType
import com.syednoufal.rideflow.core.domain.model.Trip
import com.syednoufal.rideflow.core.domain.model.TripStatus
import com.syednoufal.rideflow.core.domain.repository.TrackingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A fully simulated [TrackingRepository]. A real backend would push these
 * updates over a WebSocket or a Firebase Realtime Database channel; here a
 * repeating, timed [Flow] moves the driver from pickup to dropoff in a fixed
 * number of steps, computing ETA and route progress along the way.
 *
 * It picks up the trip where `:feature:booking`'s [com.syednoufal.rideflow.core.database.dao.RideHistoryDao]
 * row left off — see [RideHistoryEntity]'s KDoc for why this local snapshot,
 * rather than a direct module dependency, is the hand-off point between the
 * two feature modules — and regenerates the same driver deterministically
 * via [DemoDriverCatalog] so the rider sees a consistent match throughout.
 */
@Singleton
class FakeTrackingRepository @Inject constructor(
    private val rideHistoryDao: RideHistoryDao,
    private val demoDriverCatalog: DemoDriverCatalog,
    private val dispatcherProvider: DispatcherProvider,
) : TrackingRepository {

    override fun observeActiveTrip(tripId: String): Flow<Trip> = flow {
        val snapshot = rideHistoryDao.getById(tripId) ?: throw DomainException.TripNotFoundException(tripId)
        val pickup = snapshot.toPickupPoint()
        val dropoff = snapshot.toDropoffPoint()
        val fareEstimate = snapshot.toFareEstimate()
        val rideTierType = RideTierType.valueOf(snapshot.rideTierType)
        val driver = demoDriverCatalog.driverForTrip(tripId, pickup)

        repeat(PROGRESS_STEPS + 1) { step ->
            val progress = step.toFloat() / PROGRESS_STEPS
            val currentLocation = lerp(pickup, dropoff, progress)
            val remainingMinutes = (fareEstimate.estimatedDurationMinutes * (1f - progress))
                .toInt()
                .coerceAtLeast(0)

            val status = TripStatus.InProgress(
                driver = driver.copy(currentLocation = currentLocation),
                etaMinutes = remainingMinutes,
                progressFraction = progress,
            )
            emit(snapshot.toTrip(pickup, dropoff, rideTierType, fareEstimate, status))
            rideHistoryDao.upsert(snapshot.copy(status = "IN_PROGRESS"))
            delay(STEP_DELAY_MILLIS)
        }

        val completedStatus = TripStatus.Completed(
            driver = driver.copy(currentLocation = dropoff),
            finalFare = fareEstimate,
        )
        emit(snapshot.toTrip(pickup, dropoff, rideTierType, fareEstimate, completedStatus))
        rideHistoryDao.upsert(
            snapshot.copy(status = "COMPLETED", completedAtEpochMillis = System.currentTimeMillis()),
        )
    }.flowOn(dispatcherProvider.io)

    override suspend fun getTrip(tripId: String): Trip = withContext(dispatcherProvider.io) {
        val snapshot = rideHistoryDao.getById(tripId) ?: throw DomainException.TripNotFoundException(tripId)
        val pickup = snapshot.toPickupPoint()
        val dropoff = snapshot.toDropoffPoint()
        val fareEstimate = snapshot.toFareEstimate()
        val rideTierType = RideTierType.valueOf(snapshot.rideTierType)
        val driver = demoDriverCatalog.driverForTrip(tripId, pickup)

        val status: TripStatus = when (snapshot.status) {
            "COMPLETED" -> TripStatus.Completed(driver.copy(currentLocation = dropoff), fareEstimate)
            "CANCELLED" -> TripStatus.Cancelled(reason = "Cancelled")
            "IN_PROGRESS" -> TripStatus.InProgress(driver, etaMinutes = 0, progressFraction = 1f)
            else -> TripStatus.DriverArrived(driver)
        }
        snapshot.toTrip(pickup, dropoff, rideTierType, fareEstimate, status)
    }

    private fun lerp(start: GeoPoint, end: GeoPoint, fraction: Float): GeoPoint = GeoPoint(
        latitude = start.latitude + (end.latitude - start.latitude) * fraction,
        longitude = start.longitude + (end.longitude - start.longitude) * fraction,
    )

    private fun RideHistoryEntity.toPickupPoint() = GeoPoint(
        latitude = pickupLatitude,
        longitude = pickupLongitude,
        label = pickupLabel,
        address = pickupAddress,
    )

    private fun RideHistoryEntity.toDropoffPoint() = GeoPoint(
        latitude = dropoffLatitude,
        longitude = dropoffLongitude,
        label = dropoffLabel,
        address = dropoffAddress,
    )

    private fun RideHistoryEntity.toFareEstimate() = FareEstimate(
        rideTierType = RideTierType.valueOf(rideTierType),
        currencyCode = currencyCode,
        baseFare = baseFare,
        distanceFare = distanceFare,
        timeFare = timeFare,
        surgeMultiplier = surgeMultiplier,
        estimatedDistanceKm = estimatedDistanceKm,
        estimatedDurationMinutes = estimatedDurationMinutes,
    )

    private fun RideHistoryEntity.toTrip(
        pickup: GeoPoint,
        dropoff: GeoPoint,
        rideTierType: RideTierType,
        fareEstimate: FareEstimate,
        status: TripStatus,
    ) = Trip(
        id = tripId,
        pickup = pickup,
        dropoff = dropoff,
        rideTierType = rideTierType,
        fareEstimate = fareEstimate,
        status = status,
        requestedAtEpochMillis = requestedAtEpochMillis,
    )

    private companion object {
        const val PROGRESS_STEPS = 8
        const val STEP_DELAY_MILLIS = 1400L
    }
}
