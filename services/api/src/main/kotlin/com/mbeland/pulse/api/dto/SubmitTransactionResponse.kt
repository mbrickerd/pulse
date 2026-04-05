package com.mbeland.pulse.api.dto

import java.time.OffsetDateTime

data class SubmitTransactionResponse(
    val transactionId: String,
    val status: String,
    val receivedAt: OffsetDateTime,
    val customer: CustomerSummary,
    val payment: PaymentSummary,
    val risk: RiskSummary
) {
    data class CustomerSummary(
        val customerId: String,
        val maskedEmail: String
    )

    data class PaymentSummary(
        val amount: String,
        val currency: String
    )

    data class RiskSummary(
        val reviewRequired: Boolean,
        val reason: String
    )
}
