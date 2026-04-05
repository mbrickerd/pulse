package com.mbeland.pulse.processor.service

import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import com.mbeland.pulse.processor.config.RiskProperties
import com.mbeland.pulse.processor.domain.risk.DefaultRiskRules
import com.mbeland.pulse.processor.domain.risk.RiskAssessment
import com.mbeland.pulse.processor.port.PublishTransactionAssessedPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class TransactionAssessmentService(
    private val defaultRiskRules: DefaultRiskRules,
    private val publishTransactionAssessedPort: PublishTransactionAssessedPort,
    private val riskProperties: RiskProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun assess(event: TransactionSubmittedEvent) {
        log.info("Assessing transactionId={}", event.transactionId)

        val triggeredRules = defaultRiskRules.rules().mapNotNull { rule -> rule.evaluate(event) }

        val totalScore = triggeredRules.sumOf { it.scoreImpact }

        val riskLevel = when {
            totalScore >= riskProperties.riskLevelThresholds.high -> RiskAssessment.RiskLevel.HIGH
            totalScore >= riskProperties.riskLevelThresholds.medium -> RiskAssessment.RiskLevel.MEDIUM
            else -> RiskAssessment.RiskLevel.LOW
        }

        val assessment = RiskAssessment(
            transactionId = event.transactionId,
            assessedAt = OffsetDateTime.now(),
            totalScore = totalScore,
            riskLevel = riskLevel,
            reviewRequired = riskLevel != RiskAssessment.RiskLevel.LOW,
            triggeredRules = triggeredRules
        )

        log.info(
            "Assessment complete for transactionId={}, totalScore={}, riskLevel={}, triggeredRules={}",
            assessment.transactionId,
            assessment.totalScore,
            assessment.riskLevel,
            assessment.triggeredRules.map { it.code }
        )

        publishTransactionAssessedPort.publish(event, assessment)
    }
}