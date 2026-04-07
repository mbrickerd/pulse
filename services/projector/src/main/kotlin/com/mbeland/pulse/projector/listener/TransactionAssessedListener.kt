package com.mbeland.pulse.projector.listener

import com.mbeland.pulse.model.Topics
import com.mbeland.pulse.model.transaction.TransactionAssessedEvent
import com.mbeland.pulse.projector.service.TransactionProjectionService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class TransactionAssessedListener(
    private val transactionProjectionService: TransactionProjectionService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RetryableTopic(
        attempts = "4",
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_DELAY_VALUE
    )
    @KafkaListener(topics = [Topics.TRANSACTIONS_ASSESSED])
    fun onTransactionAssessed(event: TransactionAssessedEvent) {
        MDC.put("transactionId", event.transactionId)
        try {
            log.info(
                "Received transaction assessed event: transactionId={}, riskLevel={}, riskScore={}",
                event.transactionId,
                event.riskLevel,
                event.riskScore
            )
            transactionProjectionService.project(event)
        } finally {
            MDC.remove("transactionId")
        }
    }

    @DltHandler
    fun onDeadLetter(
        event: TransactionAssessedEvent,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String
    ) {
        log.error(
            "Dead letter received: transactionId={}, topic={}",
            event.transactionId,
            topic
        )
    }
}
