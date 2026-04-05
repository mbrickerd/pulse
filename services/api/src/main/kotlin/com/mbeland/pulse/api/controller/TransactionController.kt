package com.mbeland.pulse.api.controller

import com.mbeland.pulse.api.service.TransactionService
import com.mbeland.pulse.api.dto.SubmitTransactionRequest
import com.mbeland.pulse.api.dto.SubmitTransactionResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun submitTransaction(
        @Valid @RequestBody request: SubmitTransactionRequest
    ): SubmitTransactionResponse {
        return transactionService.submitTransaction(request)
    }
}