package com.mbeland.pulse.submitter.controller

import com.mbeland.pulse.submitter.service.TransactionService
import com.mbeland.pulse.submitter.dto.SubmitTransactionRequest
import com.mbeland.pulse.submitter.dto.SubmitTransactionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@Tag(name = "Transactions", description = "Submit transactions for risk assessment")
@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @Operation(
        summary = "Submit a transaction",
        description = "Publishes a transaction for asynchronous risk assessment. " +
            "Supply an idempotencyKey to safely retry without creating duplicate assessments.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "202", description = "Transaction accepted for processing",
            content = [Content(schema = Schema(implementation = SubmitTransactionResponse::class))]),
        ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
        ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = [Content()]),
        ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = [Content()]),
        ApiResponse(responseCode = "503", description = "Message broker unavailable", content = [Content()])
    )
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun submitTransaction(
        @Valid @RequestBody request: SubmitTransactionRequest,
        authentication: Authentication
    ): SubmitTransactionResponse {
        return transactionService.submitTransaction(request, authentication.name)
    }
}
