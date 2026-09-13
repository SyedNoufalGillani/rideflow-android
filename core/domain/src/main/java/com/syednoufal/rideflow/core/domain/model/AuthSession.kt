package com.syednoufal.rideflow.core.domain.model

/**
 * A pending OTP verification challenge returned after requesting a login
 * code for a phone number. [challengeId] must be echoed back, alongside the
 * code the rider enters, to [com.syednoufal.rideflow.core.domain.repository.AuthRepository.verifyOtp].
 */
data class OtpChallenge(
    val challengeId: String,
    val phoneNumber: String,
    val expiresInSeconds: Int,
)

/** The result of a successful authentication: the session token and the [user] it belongs to. */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)
