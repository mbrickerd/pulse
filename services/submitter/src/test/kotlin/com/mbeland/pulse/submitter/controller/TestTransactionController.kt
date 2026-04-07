package com.mbeland.pulse.submitter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbeland.pulse.submitter.dto.SubmitTransactionRequest
import com.mbeland.pulse.submitter.dto.SubmitTransactionResponse
import com.mbeland.pulse.submitter.interceptor.RateLimitInterceptor
import com.mbeland.pulse.submitter.service.TransactionService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.OffsetDateTime

@WebMvcTest(TransactionController::class)
class TestTransactionController {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var transactionService: TransactionService

    @MockkBean
    private lateinit var rateLimitInterceptor: RateLimitInterceptor

    @BeforeEach
    fun setUp() {
        every { rateLimitInterceptor.preHandle(any(), any(), any()) } returns true
    }

    @Test
    fun `should return 202 on valid request`() {
        every { transactionService.submitTransaction(any(), any()) } returns SubmitTransactionResponse(
            transactionId = "tx-1",
            status = "RECEIVED",
            receivedAt = OffsetDateTime.now(),
            customer = SubmitTransactionResponse.CustomerSummary("cust-1", "te***@example.com"),
            payment = SubmitTransactionResponse.PaymentSummary("100.00", "EUR")
        )

        mockMvc.post("/transactions") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validRequest())
        }.andExpect {
            status { isAccepted() }
        }
    }

    @Test
    fun `should return 401 when no token is provided`() {
        mockMvc.post("/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validRequest())
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 400 when customerId is blank`() {
        mockMvc.post("/transactions") {
            with(jwt())
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
            with(jwt())
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
    fun `should return problem detail with field errors on validation failure`() {
        val invalidRequest = validRequest().copy(
            customer = SubmitTransactionRequest.Customer(customerId = "", email = "test@example.com")
        )
        mockMvc.post("/transactions") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.title") { value("Validation Failed") }
            jsonPath("$.errors[0].field") { exists() }
            jsonPath("$.errors[0].message") { exists() }
        }
    }

    @Test
    fun `should return 400 when currency is wrong length`() {
        mockMvc.post("/transactions") {
            with(jwt())
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

    @Test
    fun `should return 429 when rate limit is exceeded`() {
        every { rateLimitInterceptor.preHandle(any(), any(), any()) } answers {
            val response = secondArg<HttpServletResponse>()
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            response.writer.write("""{"status":429,"title":"Too Many Requests","detail":"Rate limit exceeded. Please retry after a moment."}""")
            false
        }

        mockMvc.post("/transactions") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validRequest())
        }.andExpect {
            status { isTooManyRequests() }
        }
    }

    private fun validRequest() = SubmitTransactionRequest(
        customer = SubmitTransactionRequest.Customer(customerId = "cust-1", email = "test@example.com"),
        payment = SubmitTransactionRequest.Payment(amount = BigDecimal("100.00"), currency = "EUR"),
        device = SubmitTransactionRequest.Device(ipAddress = "1.2.3.4", deviceId = "dev-1", userAgent = "Mozilla"),
        billingAddress = SubmitTransactionRequest.BillingAddress(zipCode = "12345", country = "NL"),
        merchant = SubmitTransactionRequest.Merchant(merchantId = "merch-1", merchantCategory = "retail"),
        occurredAt = OffsetDateTime.parse("2024-01-01T10:00:00Z")
    )
}
