package com.mbeland.pulse.api.controller

import com.mbeland.pulse.api.dto.SubmitTransactionResponse
import com.mbeland.pulse.api.service.TransactionService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.OffsetDateTime

@WebMvcTest(TransactionController::class)
class TestTransactionController {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var transactionService: TransactionService

    @Test
    fun `should return 202 on valid request`() {
        every { transactionService.submitTransaction(any()) } returns SubmitTransactionResponse(
            transactionId = "tx-1",
            status = "RECEIVED",
            receivedAt = OffsetDateTime.now(),
            customer = SubmitTransactionResponse.CustomerSummary("cust-1", "te***@example.com"),
            payment = SubmitTransactionResponse.PaymentSummary("100.00", "EUR"),
            risk = SubmitTransactionResponse.RiskSummary(false, "No immediate risk flags at intake")
        )

        mockMvc.post("/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customer": { "customerId": "cust-1", "email": "test@example.com" },
                  "payment": { "amount": 100.00, "currency": "EUR" },
                  "device": { "ipAddress": "1.2.3.4", "deviceId": "dev-1", "userAgent": "Mozilla" },
                  "billingAddress": { "zipCode": "12345", "country": "NL" },
                  "merchant": { "merchantId": "merch-1", "merchantCategory": "retail" },
                  "occurredAt": "2024-01-01T10:00:00Z"
                }
            """.trimIndent()
        }.andExpect {
            status { isAccepted() }
        }
    }

    @Test
    fun `should return 400 when customerId is blank`() {
        mockMvc.post("/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customer": { "customerId": "", "email": "test@example.com" },
                  "payment": { "amount": 100.00, "currency": "EUR" },
                  "device": { "ipAddress": "1.2.3.4", "deviceId": "dev-1", "userAgent": "Mozilla" },
                  "billingAddress": { "zipCode": "12345", "country": "NL" },
                  "merchant": { "merchantId": "merch-1", "merchantCategory": "retail" },
                  "occurredAt": "2024-01-01T10:00:00Z"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 when amount is negative`() {
        mockMvc.post("/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customer": { "customerId": "cust-1", "email": "test@example.com" },
                  "payment": { "amount": -1, "currency": "EUR" },
                  "device": { "ipAddress": "1.2.3.4", "deviceId": "dev-1", "userAgent": "Mozilla" },
                  "billingAddress": { "zipCode": "12345", "country": "NL" },
                  "merchant": { "merchantId": "merch-1", "merchantCategory": "retail" },
                  "occurredAt": "2024-01-01T10:00:00Z"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 when currency is wrong length`() {
        mockMvc.post("/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customer": { "customerId": "cust-1", "email": "test@example.com" },
                  "payment": { "amount": 100.00, "currency": "EU" },
                  "device": { "ipAddress": "1.2.3.4", "deviceId": "dev-1", "userAgent": "Mozilla" },
                  "billingAddress": { "zipCode": "12345", "country": "NL" },
                  "merchant": { "merchantId": "merch-1", "merchantCategory": "retail" },
                  "occurredAt": "2024-01-01T10:00:00Z"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
