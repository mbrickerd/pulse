package com.mbeland.pulse.api.publisher

import com.mbeland.pulse.api.port.PublishTransactionSubmittedPort
import com.mbeland.pulse.model.Topics
import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TransactionSubmittedPublisher(
    private val kafkaTemplate: KafkaTemplate<String, TransactionSubmittedEvent>
) : PublishTransactionSubmittedPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(event: TransactionSubmittedEvent) {
        log.info(
            "Publishing TransactionSubmittedEvent for transactionId={}, customerId={}",
            event.transactionId,
            event.customer.customerId
        )

        kafkaTemplate.send(
            Topics.TRANSACTIONS_SUBMITTED,
            event.customer.customerId,
            event
        )
    }
}
