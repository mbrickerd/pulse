package com.mbeland.pulse.projector

import com.mbeland.pulse.model.Topics
import com.mbeland.pulse.projector.port.FindTransactionProjectionPort
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.core.KafkaTemplate
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@SpringBootTest
@Testcontainers
class TestTransactionProjectionIntegration {

    companion object {
        @Container @ServiceConnection @JvmField
        val kafka = KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"))

        @Container @ServiceConnection @JvmField
        val postgres = PostgreSQLContainer<Nothing>("postgres:16")
    }

    @Autowired private lateinit var kafkaTemplate: KafkaTemplate<String, Any>
    @Autowired private lateinit var findTransactionProjectionPort: FindTransactionProjectionPort

    @Test
    fun `should save projection when assessed event is received`() {
        val event = TransactionAssessedEventFactory.create()
        kafkaTemplate.send(Topics.TRANSACTIONS_ASSESSED, event.transactionId, event).get()

        await().atMost(Duration.ofSeconds(15)).until {
            findTransactionProjectionPort.findByTransactionId(event.transactionId) != null
        }

        val projection = findTransactionProjectionPort.findByTransactionId(event.transactionId)!!
        assertThat(projection.transactionId).isEqualTo(event.transactionId)
        assertThat(projection.riskLevel).isEqualTo(event.riskLevel)
        assertThat(projection.riskScore).isEqualTo(event.riskScore)
        assertThat(projection.reviewRequired).isEqualTo(event.reviewRequired)
    }

    @Test
    fun `should handle duplicate events idempotently`() {
        val event = TransactionAssessedEventFactory.create()
        kafkaTemplate.send(Topics.TRANSACTIONS_ASSESSED, event.transactionId, event).get()
        kafkaTemplate.send(Topics.TRANSACTIONS_ASSESSED, event.transactionId, event).get()

        await().atMost(Duration.ofSeconds(15)).until {
            findTransactionProjectionPort.findByTransactionId(event.transactionId) != null
        }

        assertThat(findTransactionProjectionPort.findByTransactionId(event.transactionId)).isNotNull
    }
}
