package com.mbeland.pulse.projector

import com.mbeland.pulse.model.transaction.TransactionAssessedEvent
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

object TransactionAssessedEventFactory {
    fun create(
        transactionId: String = UUID.randomUUID().toString(),
        customerId: String = "customer-123",
        amount: BigDecimal = BigDecimal("100.00"),
        currency: String = "EUR",
        riskLevel: String = "LOW",
        riskScore: Int = 0,
        reviewRequired: Boolean = false,
        reasons: List<String> = emptyList()
    ): TransactionAssessedEvent = TransactionAssessedEvent(
        eventId = UUID.randomUUID().toString(),
        eventType = "transaction.assessed",
        eventVersion = 1,
        occurredAt = OffsetDateTime.now(),
        transactionId = transactionId,
        customerId = customerId,
        amount = amount,
        currency = currency,
        riskLevel = riskLevel,
        riskScore = riskScore,
        reviewRequired = reviewRequired,
        reasons = reasons
    )
}
