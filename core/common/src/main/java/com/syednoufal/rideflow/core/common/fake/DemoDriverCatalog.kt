package com.syednoufal.rideflow.core.common.fake

import com.syednoufal.rideflow.core.domain.model.Driver
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import javax.inject.Inject
import kotlin.random.Random

/**
 * A small, fixed roster of believable driver profiles shared by every fake
 * repository that needs to hand a rider a matched [Driver] — booking's
 * driver-matching simulation and tracking's live-location simulation alike.
 * Deriving the driver deterministically from [tripId] means both fake
 * repositories (in separate feature modules, with no shared mutable state)
 * agree on the same driver for the same trip.
 */
class DemoDriverCatalog @Inject constructor() {

    /**
     * Returns a deterministic [Driver] for [tripId], initially positioned a
     * short, randomized-but-seeded distance from [nearLocation] — as if
     * driving in from a few streets away.
     */
    fun driverForTrip(tripId: String, nearLocation: GeoPoint): Driver {
        val seed = tripId.hashCode()
        val profile = PROFILES[Math.floorMod(seed, PROFILES.size)]
        val random = Random(seed)

        // Offset the starting point by a fraction of a degree — enough to
        // read as "a few blocks away" without leaving the visible map area.
        val latOffset = (random.nextFloat() - 0.5f) * OFFSET_DEGREES
        val lonOffset = (random.nextFloat() - 0.5f) * OFFSET_DEGREES

        return profile.copy(
            currentLocation = GeoPoint(
                latitude = nearLocation.latitude + latOffset,
                longitude = nearLocation.longitude + lonOffset,
            ),
        )
    }

    private companion object {
        const val OFFSET_DEGREES = 0.02

        val PROFILES = listOf(
            Driver(
                id = "driver-201",
                fullName = "Marcus Chen",
                photoUrl = null,
                rating = 4.92f,
                vehicleModel = "Toyota Camry · Silver",
                vehiclePlate = "RF-2041",
                phoneNumber = "+1 555 010 2041",
                currentLocation = GeoPoint(0.0, 0.0),
            ),
            Driver(
                id = "driver-202",
                fullName = "Priya Nair",
                photoUrl = null,
                rating = 4.98f,
                vehicleModel = "Honda Accord · Graphite",
                vehiclePlate = "RF-3387",
                phoneNumber = "+1 555 010 3387",
                currentLocation = GeoPoint(0.0, 0.0),
            ),
            Driver(
                id = "driver-203",
                fullName = "Diego Alvarez",
                photoUrl = null,
                rating = 4.87f,
                vehicleModel = "Hyundai Ioniq 5 · White",
                vehiclePlate = "RF-1129",
                phoneNumber = "+1 555 010 1129",
                currentLocation = GeoPoint(0.0, 0.0),
            ),
            Driver(
                id = "driver-204",
                fullName = "Fatima Al-Sayed",
                photoUrl = null,
                rating = 4.95f,
                vehicleModel = "Kia Carnival · Black",
                vehiclePlate = "RF-4456",
                phoneNumber = "+1 555 010 4456",
                currentLocation = GeoPoint(0.0, 0.0),
            ),
            Driver(
                id = "driver-205",
                fullName = "Sofia Kowalski",
                photoUrl = null,
                rating = 4.90f,
                vehicleModel = "Tesla Model 3 · Blue",
                vehiclePlate = "RF-7782",
                phoneNumber = "+1 555 010 7782",
                currentLocation = GeoPoint(0.0, 0.0),
            ),
        )
    }
}
