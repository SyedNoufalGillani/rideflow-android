package com.syednoufal.rideflow.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("display_label") val displayLabel: String,
    @SerialName("is_default") val isDefault: Boolean,
)

@Serializable
data class AddPaymentMethodRequestDto(
    @SerialName("type") val type: String,
    @SerialName("display_label") val displayLabel: String,
)

@Serializable
data class ChargeRequestDto(
    @SerialName("method_id") val methodId: String,
    @SerialName("amount") val amount: Double,
)

@Serializable
data class ChargeResponseDto(
    @SerialName("approved") val approved: Boolean,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("amount_charged") val amountCharged: Double? = null,
    @SerialName("decline_reason") val declineReason: String? = null,
)
