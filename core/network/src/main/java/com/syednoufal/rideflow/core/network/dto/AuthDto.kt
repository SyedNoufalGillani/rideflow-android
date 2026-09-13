package com.syednoufal.rideflow.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpRequestDto(
    @SerialName("phone_number") val phoneNumber: String,
)

@Serializable
data class OtpChallengeDto(
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("expires_in_seconds") val expiresInSeconds: Int,
)

@Serializable
data class VerifyOtpRequestDto(
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("code") val code: String,
)

@Serializable
data class AuthSessionDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user") val user: UserDto,
)

@Serializable
data class UserDto(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("email") val email: String? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
    @SerialName("rating") val rating: Float,
    @SerialName("member_since_epoch_millis") val memberSinceEpochMillis: Long,
)
