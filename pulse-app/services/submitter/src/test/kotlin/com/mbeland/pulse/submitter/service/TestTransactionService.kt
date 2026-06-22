package com.mbeland.pulse.submitter.service

import com.mbeland.pulse.submitter.dto.SubmitTransactionRequest
import com.mbeland.pulse.submitter.dto.SubmitTransactionResponse
import com.mbeland.pulse.submitter.port.PublishTransactionSubmittedPort
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.OffsetDateTime

class TestTransactionService {

    private val publishPort: PublishTransactionSubmittedPort = mockk()
    private val idempotencyService: IdempotencyService = mockk()
    private val service = TransactionService(publishPort, idempotencyService)

    private val userId = "user-123"

    private fun validRequest(email: String = "test@example.com", idempotencyKey: String? = null) = SubmitTransactionRequest(
        customer = SubmitTransactionRequest.Customer("customer-1", email),
        payment = SubmitTransactionRequest.Payment(BigDecimal("100.00"), "EUR"),
        device = SubmitTransactionRequest.Device("1.2.3.4", "device-abc", "Mozilla/5.0"),
        billingAddress = SubmitTransactionRequest.BillingAddress("12345", "NL"),
        merchant = SubmitTransactionRequest.Merchant("merchant-1", "retail"),
        occurredAt = OffsetDateTime.now(),
        idempotencyKey = idempotencyKey
    )

    @Test
    fun `should publish event and return RECEIVED status`() {
        every { idempotencyService.get(any(), any()) } returns null
        justRun { publishPort.publish(any()) }
        justRun { idempotencyService.store(any(), any(), any()) }

        val response = service.submitTransaction(validRequest(), userId)

        assertThat(response.status).isEqualTo("RECEIVED")
        verify(exactly = 1) { publishPort.publish(any()) }
    }

    @Test
    fun `should mask email correctly`() {
        justRun { publishPort.publish(any()) }

        val response = service.submitTransaction(validRequest(email = "alice@example.com"), userId)

        assertThat(response.customer.maskedEmail).isEqualTo("al***@example.com")
    }

    @Test
    fun `should return cached response and skip publishing when idempotency key matches`() {
        val cached = SubmitTransactionResponse(
            transactionId = "cached-tx",
            status = "RECEIVED",
            receivedAt = OffsetDateTime.now(),
            customer = SubmitTransactionResponse.CustomerSummary("customer-1", "te***@example.com"),
            payment = SubmitTransactionResponse.PaymentSummary("100.00", "EUR")
        )
        every { idempotencyService.get(userId, "key-abc") } returns cached

        val response = service.submitTransaction(validRequest(idempotencyKey = "key-abc"), userId)

        assertThat(response.transactionId).isEqualTo("cached-tx")
        verify(exactly = 0) { publishPort.publish(any()) }
        verify(exactly = 0) { idempotencyService.store(any(), any(), any()) }
    }

    @Test
    fun `should store response after processing when idempotency key is provided`() {
        every { idempotencyService.get(userId, "key-new") } returns null
        justRun { publishPort.publish(any()) }
        justRun { idempotencyService.store(any(), any(), any()) }

        service.submitTransaction(validRequest(idempotencyKey = "key-new"), userId)

        verify(exactly = 1) { idempotencyService.store(eq(userId), eq("key-new"), any()) }
    }

    @Test
    fun `should not interact with idempotency service when no key is provided`() {
        justRun { publishPort.publish(any()) }

        service.submitTransaction(validRequest(), userId)

        verify(exactly = 0) { idempotencyService.get(any(), any()) }
        verify(exactly = 0) { idempotencyService.store(any(), any(), any()) }
    }
}
