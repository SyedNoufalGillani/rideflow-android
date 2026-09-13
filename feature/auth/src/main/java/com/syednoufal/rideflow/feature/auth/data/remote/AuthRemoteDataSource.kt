package com.syednoufal.rideflow.feature.auth.data.remote

import com.syednoufal.rideflow.core.network.RideFlowApiService
import com.syednoufal.rideflow.core.network.dto.RequestOtpRequestDto
import com.syednoufal.rideflow.core.network.dto.VerifyOtpRequestDto
import javax.inject.Inject

/** Thin wrapper around [RideFlowApiService] for the auth endpoints. */
class AuthRemoteDataSource @Inject constructor(
    private val apiService: RideFlowApiService,
) {
    suspend fun requestOtp(phoneNumber: String) = apiService.requestOtp(RequestOtpRequestDto(phoneNumber))

    suspend fun verifyOtp(challengeId: String, code: String) =
        apiService.verifyOtp(VerifyOtpRequestDto(challengeId, code))

    suspend fun getCurrentUser() = apiService.getCurrentUser()
}
