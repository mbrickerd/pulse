package com.mbeland.pulse.processor

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbeland.pulse.model.Topics
import com.mbeland.pulse.model.transaction.TransactionAssessedEvent
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.time.Duration
import java.util.UUID

@SpringBootTest
@Testcontainers
class TestTransactionAssessmentIntegration {

    companion object {
        @Container @ServiceConnection @JvmField
        val kafka = KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"))
    }

    @Autowired private lateinit var kafkaTemplate: KafkaTemplate<String, Any>
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Value("\${spring.kafka.bootstrap-servers}") private lateinit var bootstrapServers: String

    private fun createTestConsumer(): Consumer<String, String> = KafkaConsumer(
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "integration-test-${UUID.randomUUID()}",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name
        )
    )

    @Test
    fun `should publish assessed event for low-risk transaction`() {
        val consumer = createTestConsumer()
        consumer.subscribe(listOf(Topics.TRANSACTIONS_ASSESSED))

        val event = TransactionSubmittedEventFactory.create()
        kafkaTemplate.send(Topics.TRANSACTIONS_SUBMITTED, event.customer.customerId, event).get()

        val records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(15))
        consumer.close()

        assertThat(records).isNotEmpty
        val assessed = objectMapper.readValue(records.first().value(), TransactionAssessedEvent::class.java)
        assertThat(assessed.transactionId).isEqualTo(event.transactionId)
        assertThat(assessed.riskLevel).isEqualTo("LOW")
        assertThat(assessed.riskScore).isEqualTo(0)
    }

    @Test
    fun `should publish HIGH risk assessed event for high-risk transaction`() {
        val consumer = createTestConsumer()
        consumer.subscribe(listOf(Topics.TRANSACTIONS_ASSESSED))

        val event = TransactionSubmittedEventFactory.create(
            amount = BigDecimal("5000.00"),
            country = "RU",
            merchantCategory = "crypto",
            deviceId = null
        )
        kafkaTemplate.send(Topics.TRANSACTIONS_SUBMITTED, event.customer.customerId, event).get()

        val records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(15))
        consumer.close()

        val assessed = objectMapper.readValue(records.first().value(), TransactionAssessedEvent::class.java)
        assertThat(assessed.riskLevel).isEqualTo("HIGH")
        assertThat(assessed.reviewRequired).isTrue
    }
}
