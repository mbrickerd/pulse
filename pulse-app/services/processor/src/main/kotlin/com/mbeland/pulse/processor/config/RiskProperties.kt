package com.mbeland.pulse.processor.config

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal

@Validated
@ConfigurationProperties(prefix = "pulse.risk")
data class RiskProperties(
    @field:DecimalMin("0.01")
    val highAmountThreshold: BigDecimal,
    val lowRiskCountries: Set<String>,
    val highRiskMerchantCategories: Set<String>,
    val suspiciousEmailDomains: Set<String>,

    @field:Valid
    val scoreImpacts: ScoreImpacts,

    @field:Valid
    val riskLevelThresholds: RiskLevelThresholds
) {
    data class ScoreImpacts(
        @field:Min(0)
        val highAmount: Int,

        @field:Min(0)
        val foreignBillingCountry: Int,

        @field:Min(0)
        val highRiskMerchantCategory: Int,

        @field:Min(0)
        val missingDeviceId: Int,

        @field:Min(0)
        val suspiciousEmailDomain: Int
    )

    data class RiskLevelThresholds(
        @field:Min(0)
        val medium: Int,

        @field:Min(0)
        val high: Int
    )
}
