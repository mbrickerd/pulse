package com.mbeland.pulse.projector.service

import com.mbeland.pulse.projector.port.SaveTransactionProjectionPort
import com.mbeland.pulse.projector.TransactionAssessedEventFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.math.BigDecimal

class TestTransactionProjectionService {

    private val savePort: SaveTransactionProjectionPort = mockk()
    private val service = TransactionProjectionService(savePort)

    @Test
    fun `should save projection for new event`() {
        val event = TransactionAssessedEventFactory.create(
            transactionId = "tx-123",
            customerId = "cust-456",
            amount = BigDecimal("250.00"),
            currency = "USD",
            riskLevel = "HIGH",
            riskScore = 70,
            reviewRequired = true,
            reasons = listOf("HIGH_AMOUNT", "FOREIGN_BILLING_COUNTRY")
        )
        every { savePort.save(any()) } answers { firstArg() }

        service.project(event)

        verify(exactly = 1) {
            savePort.save(match { projection ->
                projection.transactionId == "tx-123" &&
                    projection.customerId == "cust-456" &&
                    projection.riskLevel == "HIGH" &&
                    projection.riskScore == 70 &&
                    projection.reviewRequired &&
                    projection.reasons == "HIGH_AMOUNT|FOREIGN_BILLING_COUNTRY"
            })
        }
    }

    @Test
    fun `should not throw on duplicate transactionId`() {
        val event = TransactionAssessedEventFactory.create()
        every { savePort.save(any()) } throws DataIntegrityViolationException("duplicate key")

        assertThatCode { service.project(event) }.doesNotThrowAnyException()
    }
}
