package com.mbeland.pulse.projector.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbeland.pulse.model.transaction.TransactionAssessedEvent
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.ExponentialBackOffWithMaxRetries

@Configuration
class KafkaConfig(
    private val objectMapper: ObjectMapper,
    private val kafkaProperties: KafkaProperties
) {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, TransactionAssessedEvent> {
        val deserializer = JsonDeserializer(TransactionAssessedEvent::class.java, objectMapper)
        deserializer.addTrustedPackages("*")
        return DefaultKafkaConsumerFactory(
            kafkaProperties.buildConsumerProperties(null),
            StringDeserializer(),
            deserializer
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, TransactionAssessedEvent>,
        kafkaTemplate: KafkaTemplate<String, Any>
    ): ConcurrentKafkaListenerContainerFactory<String, TransactionAssessedEvent> {
        val backoff = ExponentialBackOffWithMaxRetries(4).apply {
            initialInterval = 1_000L
            multiplier = 2.0
            maxInterval = 10_000L
        }
        val errorHandler = DefaultErrorHandler(DeadLetterPublishingRecoverer(kafkaTemplate), backoff)

        return ConcurrentKafkaListenerContainerFactory<String, TransactionAssessedEvent>().apply {
            this.consumerFactory = consumerFactory
            setCommonErrorHandler(errorHandler)
            setObservationEnabled(true)
        }
    }
}
