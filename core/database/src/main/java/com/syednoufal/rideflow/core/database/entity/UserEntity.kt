package com.syednoufal.rideflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A locally cached copy of the authenticated rider's profile. */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String?,
    val profilePhotoUrl: String?,
    val rating: Float,
    val memberSinceEpochMillis: Long,
)
