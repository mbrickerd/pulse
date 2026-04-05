package com.mbeland.pulse.processor

import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

object TransactionSubmittedEventFactory {
    fun create(
        transactionId: String = UUID.randomUUID().toString(),
        customerId: String = "customer-123",
        email: String = "test@example.com",
        amount: BigDecimal = BigDecimal("100.00"),
        currency: String = "EUR",
        country: String = "NL",
        merchantCategory: String = "retail",
        deviceId: String? = "device-abc"
    ): TransactionSubmittedEvent = TransactionSubmittedEvent(
        eventId = UUID.randomUUID().toString(),
        eventType = "transaction.submitted",
        eventVersion = 1,
        occurredAt = OffsetDateTime.now(),
        transactionId = transactionId,
        customer = TransactionSubmittedEvent.Customer(customerId, email),
        payment = TransactionSubmittedEvent.Payment(amount, currency),
        device = TransactionSubmittedEvent.Device("1.2.3.4", deviceId, "Mozilla/5.0"),
        billingAddress = TransactionSubmittedEvent.BillingAddress("12345", country),
        merchant = TransactionSubmittedEvent.Merchant("merchant-1", merchantCategory)
    )
}
