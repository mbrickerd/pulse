package com.mbeland.pulse.projector.service

import com.mbeland.pulse.model.transaction.TransactionAssessedEvent
import com.mbeland.pulse.projector.domain.TransactionRiskProjection
import com.mbeland.pulse.projector.port.SaveTransactionProjectionPort
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class TransactionProjectionService(
    private val saveTransactionProjectionPort: SaveTransactionProjectionPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun project(event: TransactionAssessedEvent) {
        log.info("Projecting transactionId={}", event.transactionId)

        val projection = TransactionRiskProjection(
            transactionId = event.transactionId,
            customerId = event.customerId,
            amount = event.amount,
            currency = event.currency,
            riskLevel = event.riskLevel,
            riskScore = event.riskScore,
            reviewRequired = event.reviewRequired,
            reasons = event.reasons.joinToString("|"),
            assessedAt = event.occurredAt
        )

        try {
            saveTransactionProjectionPort.save(projection)
            log.info("Projected transactionId={}", event.transactionId)
        } catch (e: DataIntegrityViolationException) {
            log.warn("Duplicate transactionId={}, skipping idempotent event", event.transactionId)
        }
    }
}
