package com.mbeland.pulse.model.transaction

import java.math.BigDecimal
import java.time.OffsetDateTime

data class TransactionAssessedEvent(
    val eventId: String,
    val eventType: String,
    val eventVersion: Int,
    val occurredAt: OffsetDateTime,
    val transactionId: String,
    val customerId: String,
    val amount: BigDecimal,
    val currency: String,
    val riskLevel: String,
    val riskScore: Int,
    val reviewRequired: Boolean,
    val reasons: List<String>
)
