package com.mbeland.pulse.projector.controller

import com.mbeland.pulse.projector.domain.TransactionRiskProjection
import com.mbeland.pulse.projector.port.FindTransactionProjectionPort
import com.mbeland.pulse.projector.dto.TransactionRiskResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Transactions", description = "Query transaction risk assessments produced by the processor")
@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val findTransactionProjectionPort: FindTransactionProjectionPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(
        summary = "Get risk assessment by transaction ID",
        description = "Returns the risk assessment for a transaction once it has been processed. " +
            "Returns 404 if the transaction is unknown or processing has not yet completed.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Risk assessment found",
            content = [Content(schema = Schema(implementation = TransactionRiskResponse::class))]),
        ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Transaction not found or not yet assessed", content = [Content()])
    )
    @GetMapping("/{transactionId}")
    fun getTransaction(
        @Parameter(description = "The transaction ID returned by the submitter service")
        @PathVariable transactionId: String
    ): ResponseEntity<TransactionRiskResponse> {
        MDC.put("transactionId", transactionId)
        try {
            log.info("Fetching transaction transactionId={}", transactionId)
            val projection = findTransactionProjectionPort.findByTransactionId(transactionId)
                ?: return ResponseEntity.notFound().build()
            return ResponseEntity.ok(projection.toResponse())
        } finally {
            MDC.remove("transactionId")
        }
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
    reasons = reasons.map { it.reason },
    assessedAt = assessedAt
)
