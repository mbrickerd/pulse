package com.mbeland.pulse.submitter.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.OffsetDateTime

data class SubmitTransactionRequest(
    @field:Valid @field:NotNull val customer: Customer,
    @field:Valid @field:NotNull val payment: Payment,
    @field:Valid @field:NotNull val device: Device,
    @field:Valid @field:NotNull val billingAddress: BillingAddress,
    @field:Valid @field:NotNull val merchant: Merchant,
    @field:NotNull val occurredAt: OffsetDateTime,
    @field:Size(max = 64) val idempotencyKey: String? = null
) {
    data class Customer(
        @field:NotBlank val customerId: String,
        @field:NotBlank @field:Email val email: String
    )

    data class Payment(
        @field:NotNull @field:DecimalMin("0.01") val amount: BigDecimal,
        @field:NotBlank @field:Size(min = 3, max = 3) val currency: String
    )

    data class Device(
        @field:NotBlank val ipAddress: String,
        val deviceId: String?,
        val userAgent: String?
    )

    data class BillingAddress(
        @field:NotBlank val zipCode: String,
        @field:NotBlank @field:Size(min = 2, max = 2) val country: String
    )

    data class Merchant(
        @field:NotBlank val merchantId: String,
        @field:NotBlank val merchantCategory: String
    )
}
