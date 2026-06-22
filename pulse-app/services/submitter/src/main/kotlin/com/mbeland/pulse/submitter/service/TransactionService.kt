package com.mbeland.pulse.submitter.service

import com.mbeland.pulse.submitter.dto.SubmitTransactionRequest
import com.mbeland.pulse.submitter.dto.SubmitTransactionResponse
import com.mbeland.pulse.submitter.port.PublishTransactionSubmittedPort
import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class TransactionService(
    private val publishTransactionSubmittedPort: PublishTransactionSubmittedPort,
    private val idempotencyService: IdempotencyService
) {

    fun submitTransaction(request: SubmitTransactionRequest, userId: String): SubmitTransactionResponse {
        val idempotencyKey = request.idempotencyKey
        if (idempotencyKey != null) {
            val cached = idempotencyService.get(userId, idempotencyKey)
            if (cached != null) return cached
        }

        val transactionId = UUID.randomUUID().toString()
        MDC.put("transactionId", transactionId)
        try {
            val event = toEvent(transactionId, request)

            publishTransactionSubmittedPort.publish(event)

            return SubmitTransactionResponse(
                transactionId = transactionId,
                status = "RECEIVED",
                receivedAt = OffsetDateTime.now(),
                customer = SubmitTransactionResponse.CustomerSummary(
                    customerId = request.customer.customerId,
                    maskedEmail = maskEmail(request.customer.email)
                ),
                payment = SubmitTransactionResponse.PaymentSummary(
                    amount = request.payment.amount.toPlainString(),
                    currency = request.payment.currency
                )
            ).also { response ->
                if (idempotencyKey != null) {
                    idempotencyService.store(userId, idempotencyKey, response)
                }
            }
        } finally {
            MDC.remove("transactionId")
        }
    }

    private fun toEvent(transactionId: String, request: SubmitTransactionRequest): TransactionSubmittedEvent =
        TransactionSubmittedEvent(
            eventId = UUID.randomUUID().toString(),
            eventType = "transaction.submitted",
            eventVersion = 1,
            occurredAt = request.occurredAt,
            transactionId = transactionId,
            customer = TransactionSubmittedEvent.Customer(
                customerId = request.customer.customerId,
                email = request.customer.email
            ),
            payment = TransactionSubmittedEvent.Payment(
                amount = request.payment.amount,
                currency = request.payment.currency
            ),
            device = TransactionSubmittedEvent.Device(
                ipAddress = request.device.ipAddress,
                deviceId = request.device.deviceId,
                userAgent = request.device.userAgent
            ),
            billingAddress = TransactionSubmittedEvent.BillingAddress(
                zipCode = request.billingAddress.zipCode,
                country = request.billingAddress.country
            ),
            merchant = TransactionSubmittedEvent.Merchant(
                merchantId = request.merchant.merchantId,
                merchantCategory = request.merchant.merchantCategory
            )
        )

    private fun maskEmail(email: String): String {
        val parts = email.split("@", limit = 2)
        if (parts.size != 2) {
            return "***"
        }

        val localPart = parts[0]
        val domainPart = parts[1]

        val visiblePrefix = localPart.take(2)
        return "$visiblePrefix***@$domainPart"
    }
}
