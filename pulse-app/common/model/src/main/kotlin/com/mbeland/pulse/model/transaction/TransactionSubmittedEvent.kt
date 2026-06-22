package com.mbeland.pulse.model.transaction

import java.math.BigDecimal
import java.time.OffsetDateTime

data class TransactionSubmittedEvent(
    val eventId: String,
    val eventType: String,
    val eventVersion: Int,
    val occurredAt: OffsetDateTime,
    val transactionId: String,
    val customer: Customer,
    val payment: Payment,
    val device: Device,
    val billingAddress: BillingAddress,
    val merchant: Merchant
) {
    data class Customer(
        val customerId: String,
        val email: String
    )

    data class Payment(
        val amount: BigDecimal,
        val currency: String
    )

    data class Device(
        val ipAddress: String,
        val deviceId: String?,
        val userAgent: String?
    )

    data class BillingAddress(
        val zipCode: String,
        val country: String
    )

    data class Merchant(
        val merchantId: String,
        val merchantCategory: String
    )
}
