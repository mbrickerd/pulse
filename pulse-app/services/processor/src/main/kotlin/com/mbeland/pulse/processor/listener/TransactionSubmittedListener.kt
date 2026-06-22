package com.mbeland.pulse.processor.listener

import com.mbeland.pulse.model.Topics
import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import com.mbeland.pulse.processor.service.TransactionAssessmentService
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
class TransactionSubmittedListener(
    private val transactionAssessmentService: TransactionAssessmentService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RetryableTopic(
        attempts = "4",
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_DELAY_VALUE
    )
    @KafkaListener(topics = [Topics.TRANSACTIONS_SUBMITTED])
    fun onTransactionSubmitted(event: TransactionSubmittedEvent) {
        MDC.put("transactionId", event.transactionId)
        try {
            log.info(
                "Received transaction submitted event: transactionId={}, customerId={}, amount={}, currency={}",
                event.transactionId,
                event.customer.customerId,
                event.payment.amount,
                event.payment.currency
            )
            transactionAssessmentService.assess(event)
        } finally {
            MDC.remove("transactionId")
        }
    }

    @DltHandler
    fun onDeadLetter(
        event: TransactionSubmittedEvent,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String
    ) {
        log.error(
            "Dead letter received: transactionId={}, topic={}",
            event.transactionId,
            topic
        )
    }
}
