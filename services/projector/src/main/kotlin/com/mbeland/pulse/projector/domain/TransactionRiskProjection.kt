package com.mbeland.pulse.projector.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Table("transaction_risk_projection")
data class TransactionRiskProjection(
    @Id val id: Long? = null,
    val transactionId: String,
    val customerId: String,
    val amount: BigDecimal,
    val currency: String,
    val riskLevel: String,
    val riskScore: Int,
    val reviewRequired: Boolean,
    val reasons: String,
    val assessedAt: OffsetDateTime
)
