package com.mbeland.pulse.processor.domain.risk

import java.time.OffsetDateTime

data class RiskAssessment(
    val transactionId: String,
    val assessedAt: OffsetDateTime,
    val totalScore: Int,
    val riskLevel: RiskLevel,
    val reviewRequired: Boolean,
    val triggeredRules: List<RuleResult>
) {
    enum class RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }

    val reasons: List<String>
        get() = triggeredRules.map { it.reason }
}