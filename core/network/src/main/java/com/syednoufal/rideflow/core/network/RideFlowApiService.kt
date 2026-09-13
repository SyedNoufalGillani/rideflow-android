package com.syednoufal.rideflow.core.network

import com.syednoufal.rideflow.core.network.dto.AddPaymentMethodRequestDto
import com.syednoufal.rideflow.core.network.dto.AuthSessionDto
import com.syednoufal.rideflow.core.network.dto.ChargeRequestDto
import com.syednoufal.rideflow.core.network.dto.ChargeResponseDto
import com.syednoufal.rideflow.core.network.dto.FareEstimateDto
import com.syednoufal.rideflow.core.network.dto.FareEstimateRequestDto
import com.syednoufal.rideflow.core.network.dto.OtpChallengeDto
import com.syednoufal.rideflow.core.network.dto.PaymentMethodDto
import com.syednoufal.rideflow.core.network.dto.RequestOtpRequestDto
import com.syednoufal.rideflow.core.network.dto.RideRequestDto
import com.syednoufal.rideflow.core.network.dto.RideTierDto
import com.syednoufal.rideflow.core.network.dto.TripDto
import com.syednoufal.rideflow.core.network.dto.UserDto
import com.syednoufal.rideflow.core.network.dto.VerifyOtpRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit contract for a real RideFlow backend. Nothing in this project
 * calls these methods by default — see [com.syednoufal.rideflow.core.network.NetworkConstants.BASE_URL] —
 * but the interface is a complete, realistic surface a production backend
 * integration would implement against.
 */
interface RideFlowApiService {

    @POST("v1/auth/otp/request")
    suspend fun requestOtp(@Body request: RequestOtpRequestDto): OtpChallengeDto

    @POST("v1/auth/otp/verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequestDto): AuthSessionDto

    @GET("v1/auth/me")
    suspend fun getCurrentUser(): UserDto

    @GET("v1/booking/ride-tiers")
    suspend fun getRideTiers(): List<RideTierDto>

    @POST("v1/booking/fare-estimates")
    suspend fun getFareEstimates(@Body request: FareEstimateRequestDto): List<FareEstimateDto>

    @POST("v1/booking/rides")
    suspend fun requestRide(@Body request: RideRequestDto): TripDto

    @GET("v1/booking/rides/{tripId}")
    suspend fun getTrip(@Path("tripId") tripId: String): TripDto

    @PATCH("v1/booking/rides/{tripId}/cancel")
    suspend fun cancelRide(@Path("tripId") tripId: String)

    @GET("v1/payments/methods")
    suspend fun getPaymentMethods(): List<PaymentMethodDto>

    @POST("v1/payments/methods")
    suspend fun addPaymentMethod(@Body request: AddPaymentMethodRequestDto): PaymentMethodDto

    @PATCH("v1/payments/methods/{methodId}/default")
    suspend fun setDefaultPaymentMethod(@Path("methodId") methodId: String)

    @POST("v1/payments/charge")
    suspend fun charge(@Body request: ChargeRequestDto): ChargeResponseDto
}
