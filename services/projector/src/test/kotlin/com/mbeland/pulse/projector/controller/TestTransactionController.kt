package com.mbeland.pulse.projector.controller

import com.mbeland.pulse.projector.domain.TransactionRiskProjection
import com.mbeland.pulse.projector.port.FindTransactionProjectionPort
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal
import java.time.OffsetDateTime

@WebMvcTest(TransactionController::class)
class TestTransactionController {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var findTransactionProjectionPort: FindTransactionProjectionPort

    @Test
    fun `should return 200 with projection when transactionId exists`() {
        val projection = TransactionRiskProjection(
            id = 1L,
            transactionId = "tx-123",
            customerId = "cust-456",
            amount = BigDecimal("100.00"),
            currency = "EUR",
            riskLevel = "LOW",
            riskScore = 0,
            reviewRequired = false,
            reasons = "",
            assessedAt = OffsetDateTime.parse("2024-01-01T10:00:00Z")
        )
        every { findTransactionProjectionPort.findByTransactionId("tx-123") } returns projection

        mockMvc.get("/transactions/tx-123") {
            with(jwt())
        }.andExpect {
            status { isOk() }
            jsonPath("$.transactionId") { value("tx-123") }
            jsonPath("$.customerId") { value("cust-456") }
            jsonPath("$.riskLevel") { value("LOW") }
        }
    }

    @Test
    fun `should return 401 when no token is provided`() {
        mockMvc.get("/transactions/tx-123").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 404 when transactionId not found`() {
        every { findTransactionProjectionPort.findByTransactionId("unknown") } returns null

        mockMvc.get("/transactions/unknown") {
            with(jwt())
        }.andExpect {
            status { isNotFound() }
        }
    }
}
