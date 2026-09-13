package com.syednoufal.rideflow.feature.booking.data.repository

import com.syednoufal.rideflow.core.common.dispatcher.DispatcherProvider
import com.syednoufal.rideflow.core.common.fake.DemoDriverCatalog
import com.syednoufal.rideflow.core.database.dao.RideHistoryDao
import com.syednoufal.rideflow.core.database.dao.SavedPlaceDao
import com.syednoufal.rideflow.core.database.entity.RideHistoryEntity
import com.syednoufal.rideflow.core.database.entity.SavedPlaceEntity
import com.syednoufal.rideflow.core.domain.model.DomainException
import com.syednoufal.rideflow.core.domain.model.FareEstimate
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.RideTier
import com.syednoufal.rideflow.core.domain.model.RideTierType
import com.syednoufal.rideflow.core.domain.model.Trip
import com.syednoufal.rideflow.core.domain.model.TripStatus
import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * A fully simulated [BookingRepository]. Prices routes with a believable
 * distance/time model, and simulates driver matching with realistic timed
 * delays instead of calling a real dispatch backend.
 */
@Singleton
class FakeBookingRepository @Inject constructor(
    private val savedPlaceDao: SavedPlaceDao,
    private val rideHistoryDao: RideHistoryDao,
    private val demoDriverCatalog: DemoDriverCatalog,
    private val dispatcherProvider: DispatcherProvider,
) : BookingRepository {

    private val activeRides = ConcurrentHashMap<String, PendingRide>()

    override suspend fun getRideTiers(): List<RideTier> = withContext(dispatcherProvider.default) {
        listOf(
            RideTier(
                type = RideTierType.ECONOMY,
                displayName = "Economy",
                description = "Affordable, everyday rides",
                capacity = 4,
                baseFareMultiplier = 1.0,
                etaMinutes = 3,
            ),
            RideTier(
                type = RideTierType.COMFORT,
                displayName = "Comfort",
                description = "Newer cars, extra legroom",
                capacity = 4,
                baseFareMultiplier = 1.3,
                etaMinutes = 4,
            ),
            RideTier(
                type = RideTierType.XL,
                displayName = "XL",
                description = "Bigger cars for groups up to 6",
                capacity = 6,
                baseFareMultiplier = 1.6,
                etaMinutes = 5,
            ),
            RideTier(
                type = RideTierType.PREMIUM,
                displayName = "Premium",
                description = "Top-rated drivers, luxury vehicles",
                capacity = 4,
                baseFareMultiplier = 2.2,
                etaMinutes = 6,
            ),
        )
    }

    override suspend fun getSavedPlaces(): List<GeoPoint> = withContext(dispatcherProvider.io) {
        seedSavedPlacesIfEmpty()
        savedPlaceDao.getAll().map { it.toDomain() }
    }

    override suspend fun getFareEstimates(pickup: GeoPoint, dropoff: GeoPoint): List<FareEstimate> =
        withContext(dispatcherProvider.default) {
            delay(FARE_ESTIMATE_LATENCY_MILLIS)
            val distanceKm = pickup.distanceKmTo(dropoff).coerceAtLeast(MIN_BILLABLE_DISTANCE_KM)
            val durationMinutes = ((distanceKm / AVERAGE_SPEED_KMH) * MINUTES_PER_HOUR).toInt().coerceAtLeast(1)
            val random = Random(seedFor(pickup, dropoff))
            val surgeMultiplier = if (random.nextFloat() < SURGE_CHANCE) SURGE_MULTIPLIER else 1.0

            getRideTiers().map { tier ->
                FareEstimate(
                    rideTierType = tier.type,
                    currencyCode = CURRENCY_CODE,
                    baseFare = BASE_FARE * tier.baseFareMultiplier,
                    distanceFare = distanceKm * PER_KM_RATE * tier.baseFareMultiplier,
                    timeFare = durationMinutes * PER_MINUTE_RATE * tier.baseFareMultiplier,
                    surgeMultiplier = surgeMultiplier,
                    estimatedDistanceKm = distanceKm,
                    estimatedDurationMinutes = durationMinutes,
                )
            }
        }

    override suspend fun requestRide(
        pickup: GeoPoint,
        dropoff: GeoPoint,
        rideTierType: RideTierType,
        fareEstimate: FareEstimate,
    ): Trip = withContext(dispatcherProvider.io) {
        delay(REQUEST_RIDE_LATENCY_MILLIS)
        val tripId = UUID.randomUUID().toString()
        val requestedAt = System.currentTimeMillis()

        val pendingRide = PendingRide(pickup, dropoff, rideTierType, fareEstimate, requestedAt)
        activeRides[tripId] = pendingRide
        rideHistoryDao.upsert(historyRowFor(tripId, pendingRide, status = "SEARCHING"))

        Trip(
            id = tripId,
            pickup = pickup,
            dropoff = dropoff,
            rideTierType = rideTierType,
            fareEstimate = fareEstimate,
            status = TripStatus.SearchingForDriver,
            requestedAtEpochMillis = requestedAt,
        )
    }

    override fun observeDriverMatching(tripId: String): Flow<Trip> = flow {
        val pendingRide = activeRides[tripId] ?: throw DomainException.TripNotFoundException(tripId)

        fun tripWith(status: TripStatus) = Trip(
            id = tripId,
            pickup = pendingRide.pickup,
            dropoff = pendingRide.dropoff,
            rideTierType = pendingRide.rideTierType,
            fareEstimate = pendingRide.fareEstimate,
            status = status,
            requestedAtEpochMillis = pendingRide.requestedAtEpochMillis,
        )

        emit(tripWith(TripStatus.SearchingForDriver))
        delay(SEARCH_PHASE_MILLIS)

        val driver = demoDriverCatalog.driverForTrip(tripId, pendingRide.pickup)
        emit(tripWith(TripStatus.DriverAssigned(driver, etaMinutes = DRIVER_APPROACH_ETA_MINUTES)))
        rideHistoryDao.upsert(historyRowFor(tripId, pendingRide, status = "DRIVER_ASSIGNED"))
        delay(DRIVER_APPROACH_PHASE_MILLIS)

        emit(tripWith(TripStatus.DriverArrived(driver)))
        rideHistoryDao.upsert(historyRowFor(tripId, pendingRide, status = "DRIVER_ARRIVED"))
    }.flowOn(dispatcherProvider.io)

    override suspend fun cancelRide(tripId: String) = withContext(dispatcherProvider.io) {
        val pendingRide = activeRides.remove(tripId)
        if (pendingRide != null) {
            rideHistoryDao.upsert(historyRowFor(tripId, pendingRide, status = "CANCELLED"))
        }
    }

    private fun historyRowFor(tripId: String, pendingRide: PendingRide, status: String) = RideHistoryEntity(
        tripId = tripId,
        pickupLabel = pendingRide.pickup.label ?: pendingRide.pickup.address,
        pickupAddress = pendingRide.pickup.address,
        pickupLatitude = pendingRide.pickup.latitude,
        pickupLongitude = pendingRide.pickup.longitude,
        dropoffLabel = pendingRide.dropoff.label ?: pendingRide.dropoff.address,
        dropoffAddress = pendingRide.dropoff.address,
        dropoffLatitude = pendingRide.dropoff.latitude,
        dropoffLongitude = pendingRide.dropoff.longitude,
        rideTierType = pendingRide.rideTierType.name,
        baseFare = pendingRide.fareEstimate.baseFare,
        distanceFare = pendingRide.fareEstimate.distanceFare,
        timeFare = pendingRide.fareEstimate.timeFare,
        surgeMultiplier = pendingRide.fareEstimate.surgeMultiplier,
        estimatedDistanceKm = pendingRide.fareEstimate.estimatedDistanceKm,
        estimatedDurationMinutes = pendingRide.fareEstimate.estimatedDurationMinutes,
        currencyCode = pendingRide.fareEstimate.currencyCode,
        status = status,
        requestedAtEpochMillis = pendingRide.requestedAtEpochMillis,
        completedAtEpochMillis = null,
    )

    private suspend fun seedSavedPlacesIfEmpty() {
        if (savedPlaceDao.getAll().isNotEmpty()) return
        savedPlaceDao.upsertAll(DEFAULT_SAVED_PLACES)
    }

    private fun seedFor(pickup: GeoPoint, dropoff: GeoPoint): Int =
        31 * pickup.hashCode() + dropoff.hashCode()

    private fun SavedPlaceEntity.toDomain() = GeoPoint(
        latitude = latitude,
        longitude = longitude,
        label = label,
        address = address,
    )

    private data class PendingRide(
        val pickup: GeoPoint,
        val dropoff: GeoPoint,
        val rideTierType: RideTierType,
        val fareEstimate: FareEstimate,
        val requestedAtEpochMillis: Long,
    )

    private companion object {
        const val CURRENCY_CODE = "USD"
        const val BASE_FARE = 2.5
        const val PER_KM_RATE = 1.35
        const val PER_MINUTE_RATE = 0.28
        const val AVERAGE_SPEED_KMH = 28.0
        const val MINUTES_PER_HOUR = 60
        const val MIN_BILLABLE_DISTANCE_KM = 0.5
        const val SURGE_CHANCE = 0.2f
        const val SURGE_MULTIPLIER = 1.35
        const val FARE_ESTIMATE_LATENCY_MILLIS = 600L
        const val REQUEST_RIDE_LATENCY_MILLIS = 500L
        const val SEARCH_PHASE_MILLIS = 2600L
        const val DRIVER_APPROACH_PHASE_MILLIS = 3200L
        const val DRIVER_APPROACH_ETA_MINUTES = 4

        val DEFAULT_SAVED_PLACES = listOf(
            SavedPlaceEntity(
                id = "place-downtown-loop",
                label = "Downtown Loop",
                address = "1 Civic Plaza",
                latitude = 37.7955,
                longitude = -122.3937,
                isFavorite = true,
                lastUsedEpochMillis = System.currentTimeMillis(),
            ),
            SavedPlaceEntity(
                id = "place-airport",
                label = "RideFlow Airport Terminal",
                address = "900 Terminal Way",
                latitude = 37.6213,
                longitude = -122.3790,
                isFavorite = true,
                lastUsedEpochMillis = System.currentTimeMillis(),
            ),
            SavedPlaceEntity(
                id = "place-tech-campus",
                label = "Harborview Tech Campus",
                address = "400 Innovation Dr",
                latitude = 37.4275,
                longitude = -122.1697,
                isFavorite = false,
                lastUsedEpochMillis = System.currentTimeMillis(),
            ),
            SavedPlaceEntity(
                id = "place-central-park",
                label = "Central Greenway Park",
                address = "88 Parkside Ave",
                latitude = 37.7694,
                longitude = -122.4862,
                isFavorite = false,
                lastUsedEpochMillis = System.currentTimeMillis(),
            ),
        )
    }
}
