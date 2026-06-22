package com.mbeland.pulse.projector.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class TransactionRiskResponse(
    val transactionId: String,
    val customerId: String,
    val amount: BigDecimal,
    val currency: String,
    val riskLevel: String,
    val riskScore: Int,
    val reviewRequired: Boolean,
    val reasons: List<String>,
    val assessedAt: OffsetDateTime
)
