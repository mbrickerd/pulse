package com.mbeland.pulse.api.service

import com.mbeland.pulse.api.config.RiskProperties
import com.mbeland.pulse.api.dto.SubmitTransactionRequest
import com.mbeland.pulse.api.port.PublishTransactionSubmittedPort
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.OffsetDateTime

class TestTransactionService {

    private val publishPort: PublishTransactionSubmittedPort = mockk()
    private val riskProperties = RiskProperties(manualReviewThreshold = BigDecimal("500.00"))
    private val service = TransactionService(riskProperties, publishPort)

    private fun validRequest(
        amount: BigDecimal = BigDecimal("100.00"),
        email: String = "test@example.com"
    ) = SubmitTransactionRequest(
        customer = SubmitTransactionRequest.Customer("customer-1", email),
        payment = SubmitTransactionRequest.Payment(amount, "EUR"),
        device = SubmitTransactionRequest.Device("1.2.3.4", "device-abc", "Mozilla/5.0"),
        billingAddress = SubmitTransactionRequest.BillingAddress("12345", "NL"),
        merchant = SubmitTransactionRequest.Merchant("merchant-1", "retail"),
        occurredAt = OffsetDateTime.now()
    )

    @Test
    fun `should publish event and return RECEIVED status`() {
        justRun { publishPort.publish(any()) }

        val response = service.submitTransaction(validRequest())

        assertThat(response.status).isEqualTo("RECEIVED")
        verify(exactly = 1) { publishPort.publish(any()) }
    }

    @Test
    fun `should set reviewRequired true when amount meets threshold`() {
        justRun { publishPort.publish(any()) }

        val response = service.submitTransaction(validRequest(amount = BigDecimal("500.00")))

        assertThat(response.risk.reviewRequired).isTrue()
    }

    @Test
    fun `should set reviewRequired false when amount is below threshold`() {
        justRun { publishPort.publish(any()) }

        val response = service.submitTransaction(validRequest(amount = BigDecimal("499.99")))

        assertThat(response.risk.reviewRequired).isFalse()
    }

    @Test
    fun `should mask email correctly`() {
        justRun { publishPort.publish(any()) }

        val response = service.submitTransaction(validRequest(email = "alice@example.com"))

        assertThat(response.customer.maskedEmail).isEqualTo("al***@example.com")
    }
}
