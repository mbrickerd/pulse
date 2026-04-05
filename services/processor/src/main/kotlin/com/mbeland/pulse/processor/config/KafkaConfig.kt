package com.mbeland.pulse.processor.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class KafkaConfig(
    private val objectMapper: ObjectMapper,
    private val kafkaProperties: KafkaProperties
) {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, TransactionSubmittedEvent> {
        val deserializer = JsonDeserializer(TransactionSubmittedEvent::class.java, objectMapper)
        deserializer.addTrustedPackages("*")
        return DefaultKafkaConsumerFactory(
            kafkaProperties.buildConsumerProperties(null),
            StringDeserializer(),
            deserializer
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, TransactionSubmittedEvent>
    ): ConcurrentKafkaListenerContainerFactory<String, TransactionSubmittedEvent> {
        return ConcurrentKafkaListenerContainerFactory<String, TransactionSubmittedEvent>().apply {
            this.consumerFactory = consumerFactory
        }
    }
}
