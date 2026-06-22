package com.mbeland.pulse.processor.domain.risk

import com.mbeland.pulse.processor.config.RiskProperties
import com.mbeland.pulse.processor.TransactionSubmittedEventFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestDefaultRiskRules {

    private val riskProperties = RiskProperties(
        highAmountThreshold = BigDecimal("1000.00"),
        lowRiskCountries = setOf("NL", "DE", "FR"),
        highRiskMerchantCategories = setOf("gambling", "crypto"),
        suspiciousEmailDomains = setOf("tempmail.com", "disposable.io"),
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

    private val defaultRiskRules = DefaultRiskRules(riskProperties)

    @Test
    fun `highAmountRule triggers when amount exceeds threshold`() {
        val event = TransactionSubmittedEventFactory.create(amount = BigDecimal("1000.00"))

        val result = defaultRiskRules.rules()[0].evaluate(event)

        assertThat(result).isNotNull()
        assertThat(result!!.code).isEqualTo("HIGH_AMOUNT")
    }

    @Test
    fun `highAmountRule does not trigger below threshold`() {
        val event = TransactionSubmittedEventFactory.create(amount = BigDecimal("999.99"))

        val result = defaultRiskRules.rules()[0].evaluate(event)

        assertThat(result).isNull()
    }

    @Test
    fun `foreignBillingCountryRule triggers for unlisted country`() {
        val event = TransactionSubmittedEventFactory.create(country = "US")

        val result = defaultRiskRules.rules()[1].evaluate(event)

        assertThat(result).isNotNull()
        assertThat(result!!.code).isEqualTo("FOREIGN_BILLING_COUNTRY")
    }

    @Test
    fun `foreignBillingCountryRule does not trigger for listed country`() {
        val event = TransactionSubmittedEventFactory.create(country = "NL")

        val result = defaultRiskRules.rules()[1].evaluate(event)

        assertThat(result).isNull()
    }

    @Test
    fun `highRiskMerchantCategoryRule triggers for listed category`() {
        val event = TransactionSubmittedEventFactory.create(merchantCategory = "gambling")

        val result = defaultRiskRules.rules()[2].evaluate(event)

        assertThat(result).isNotNull()
        assertThat(result!!.code).isEqualTo("HIGH_RISK_MERCHANT_CATEGORY")
    }

    @Test
    fun `missingDeviceIdRule triggers when deviceId is null`() {
        val event = TransactionSubmittedEventFactory.create(deviceId = null)

        val result = defaultRiskRules.rules()[3].evaluate(event)

        assertThat(result).isNotNull()
        assertThat(result!!.code).isEqualTo("MISSING_DEVICE_ID")
    }

    @Test
    fun `missingDeviceIdRule does not trigger when deviceId is present`() {
        val event = TransactionSubmittedEventFactory.create(deviceId = "device-123")

        val result = defaultRiskRules.rules()[3].evaluate(event)

        assertThat(result).isNull()
    }

    @Test
    fun `suspiciousEmailDomainRule triggers for listed domain`() {
        val event = TransactionSubmittedEventFactory.create(email = "user@tempmail.com")

        val result = defaultRiskRules.rules()[4].evaluate(event)

        assertThat(result).isNotNull()
        assertThat(result!!.code).isEqualTo("SUSPICIOUS_EMAIL_DOMAIN")
    }
}
