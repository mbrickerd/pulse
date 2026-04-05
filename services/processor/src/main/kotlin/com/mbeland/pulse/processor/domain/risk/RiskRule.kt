package com.mbeland.pulse.processor.domain.risk

import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent

fun interface RiskRule {
    fun evaluate(event: TransactionSubmittedEvent): RuleResult?
}