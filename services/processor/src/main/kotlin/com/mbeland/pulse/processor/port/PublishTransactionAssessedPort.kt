package com.mbeland.pulse.processor.port

import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import com.mbeland.pulse.processor.domain.risk.RiskAssessment

interface PublishTransactionAssessedPort {
    fun publish(submittedEvent: TransactionSubmittedEvent, assessment: RiskAssessment)
}