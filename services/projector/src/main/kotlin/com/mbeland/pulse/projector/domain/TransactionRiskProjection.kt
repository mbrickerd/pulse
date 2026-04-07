package com.mbeland.pulse.projector.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Table("transaction_risk_projection")
class TransactionRiskProjection(
    @Id val id: Long? = null,
    val transactionId: String,
    val customerId: String,
    val amount: BigDecimal,
    val currency: String,
    val riskLevel: String,
    val riskScore: Int,
    val reviewRequired: Boolean,
    @MappedCollection(idColumn = "projection_id", keyColumn = "position")
    val reasons: List<TransactionReason>,
    val assessedAt: OffsetDateTime
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionRiskProjection) return false
        return transactionId == other.transactionId
    }

    override fun hashCode(): Int = transactionId.hashCode()
}
