package com.syednoufal.rideflow.core.domain.model

/**
 * The authenticated rider. [id] and [phoneNumber] are the only fields
 * guaranteed to be present; the rest are populated once profile data syncs.
 */
data class User(
    val id: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String?,
    val profilePhotoUrl: String?,
    val rating: Float,
    val memberSinceEpochMillis: Long,
)
