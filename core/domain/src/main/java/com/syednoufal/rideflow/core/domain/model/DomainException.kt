package com.syednoufal.rideflow.core.domain.model

/** Base type for expected, presentable domain failures — as opposed to unexpected bugs/crashes. */
sealed class DomainException(message: String) : Exception(message) {

    /** The OTP code entered did not match the pending challenge, or the challenge expired. */
    class InvalidOtpException(message: String = "That code didn't match. Please try again.") :
        DomainException(message)

    /** No driver could be matched for a ride request within a reasonable time. */
    class NoDriverAvailableException(message: String = "No drivers are available right now.") :
        DomainException(message)

    /** A payment gateway declined a charge attempt. */
    class PaymentDeclinedException(message: String) : DomainException(message)

    /** A requested trip does not exist or does not belong to the current rider. */
    class TripNotFoundException(tripId: String) : DomainException("Trip $tripId was not found.")
}
