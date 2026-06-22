package com.mbeland.pulse.processor.publisher

import com.mbeland.pulse.model.Topics
import com.mbeland.pulse.model.transaction.TransactionAssessedEvent
import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import com.mbeland.pulse.processor.domain.risk.RiskAssessment
import com.mbeland.pulse.processor.port.PublishTransactionAssessedPort
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TransactionAssessedPublisher(
    private val kafkaTemplate: KafkaTemplate<String, TransactionAssessedEvent>
) : PublishTransactionAssessedPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(submittedEvent: TransactionSubmittedEvent, assessment: RiskAssessment) {
        val event = TransactionAssessedEvent(
            eventId = UUID.randomUUID().toString(),
            eventType = "transaction.assessed",
            eventVersion = 1,
            occurredAt = assessment.assessedAt,
            transactionId = assessment.transactionId,
            customerId = submittedEvent.customer.customerId,
            amount = submittedEvent.payment.amount,
            currency = submittedEvent.payment.currency,
            riskLevel = assessment.riskLevel.name,
            riskScore = assessment.totalScore,
            reviewRequired = assessment.reviewRequired,
            reasons = assessment.reasons
        )

        log.info(
            "Publishing TransactionAssessedEvent for transactionId={}, riskScore={}, riskLevel={}",
            event.transactionId,
            event.riskScore,
            event.riskLevel
        )

        kafkaTemplate.send(
            Topics.TRANSACTIONS_ASSESSED,
            event.transactionId,
            event
        ).get()
    }
}
