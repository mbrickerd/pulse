package com.mbeland.pulse.processor.service

import com.mbeland.pulse.processor.config.RiskProperties
import com.mbeland.pulse.processor.domain.risk.DefaultRiskRules
import com.mbeland.pulse.processor.domain.risk.RiskAssessment
import com.mbeland.pulse.processor.domain.risk.RiskRule
import com.mbeland.pulse.processor.domain.risk.RuleResult
import com.mbeland.pulse.processor.port.PublishTransactionAssessedPort
import com.mbeland.pulse.processor.TransactionSubmittedEventFactory
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestTransactionAssessmentService {

    private val defaultRiskRules: DefaultRiskRules = mockk()
    private val publishPort: PublishTransactionAssessedPort = mockk()
    private val riskProperties = RiskProperties(
        highAmountThreshold = BigDecimal("1000.00"),
        lowRiskCountries = setOf("NL"),
        highRiskMerchantCategories = setOf("gambling"),
        suspiciousEmailDomains = setOf("tempmail.com"),
        scoreImpacts = RiskProperties.ScoreImpacts(
            highAmount = 30,
            foreignBillingCountry = 20,
            highRiskMerchantCategory = 25,
            missingDeviceId = 15,
            suspiciousEmailDomain = 20
        ),
        riskLevelThresholds = RiskProperties.RiskLevelThresholds(
            medium = 30,
            high = 60
        )
    )
    private val service = TransactionAssessmentService(defaultRiskRules, publishPort, riskProperties)

    @Test
    fun `should publish assessment for submitted event`() {
        val event = TransactionSubmittedEventFactory.create()
        every { defaultRiskRules.rules() } returns emptyList()
        justRun { publishPort.publish(any(), any()) }

        service.assess(event)

        verify(exactly = 1) { publishPort.publish(event, any()) }
    }

    @Test
    fun `should assign HIGH risk level when score meets high threshold`() {
        val event = TransactionSubmittedEventFactory.create()
        val assessmentSlot = slot<RiskAssessment>()
        every { defaultRiskRules.rules() } returns listOf(
            RiskRule { _ -> RuleResult("HIGH_AMOUNT", "High amount", 60) }
        )
        justRun { publishPort.publish(any(), capture(assessmentSlot)) }

        service.assess(event)

        assertThat(assessmentSlot.captured.riskLevel).isEqualTo(RiskAssessment.RiskLevel.HIGH)
    }

    @Test
    fun `should assign MEDIUM risk level when score is between thresholds`() {
        val event = TransactionSubmittedEventFactory.create()
        val assessmentSlot = slot<RiskAssessment>()
        every { defaultRiskRules.rules() } returns listOf(
            RiskRule { _ -> RuleResult("FOREIGN_BILLING_COUNTRY", "Foreign country", 40) }
        )
        justRun { publishPort.publish(any(), capture(assessmentSlot)) }

        service.assess(event)

        assertThat(assessmentSlot.captured.riskLevel).isEqualTo(RiskAssessment.RiskLevel.MEDIUM)
    }

    @Test
    fun `should assign LOW risk level when no rules trigger`() {
        val event = TransactionSubmittedEventFactory.create()
        val assessmentSlot = slot<RiskAssessment>()
        every { defaultRiskRules.rules() } returns emptyList()
        justRun { publishPort.publish(any(), capture(assessmentSlot)) }

        service.assess(event)

        assertThat(assessmentSlot.captured.riskLevel).isEqualTo(RiskAssessment.RiskLevel.LOW)
    }
}
