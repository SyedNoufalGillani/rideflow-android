package com.syednoufal.rideflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A pickup/dropoff location the rider has saved or recently used. */
@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey val id: String,
    val label: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean,
    val lastUsedEpochMillis: Long,
)
