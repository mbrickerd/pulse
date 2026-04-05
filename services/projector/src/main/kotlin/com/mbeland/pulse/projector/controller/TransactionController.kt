package com.mbeland.pulse.projector.controller

import com.mbeland.pulse.projector.domain.TransactionRiskProjection
import com.mbeland.pulse.projector.port.FindTransactionProjectionPort
import com.mbeland.pulse.projector.dto.TransactionRiskResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val findTransactionProjectionPort: FindTransactionProjectionPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/{transactionId}")
    fun getTransaction(@PathVariable transactionId: String): ResponseEntity<TransactionRiskResponse> {
        log.info("Fetching transaction transactionId={}", transactionId)
        val projection = findTransactionProjectionPort.findByTransactionId(transactionId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(projection.toResponse())
    }
}

private fun TransactionRiskProjection.toResponse() = TransactionRiskResponse(
    transactionId = transactionId,
    customerId = customerId,
    amount = amount,
    currency = currency,
    riskLevel = riskLevel,
    riskScore = riskScore,
    reviewRequired = reviewRequired,
    reasons = reasons.split("|").filter { it.isNotBlank() },
    assessedAt = assessedAt
)
