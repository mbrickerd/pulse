package com.mbeland.pulse.processor.domain.risk

import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent
import com.mbeland.pulse.processor.config.RiskProperties
import org.springframework.stereotype.Component

@Component
class DefaultRiskRules(private val riskProperties: RiskProperties) {

    fun rules(): List<RiskRule> =
        listOf(
            highAmountRule(),
            foreignBillingCountryRule(),
            highRiskMerchantCategoryRule(),
            missingDeviceIdRule(),
            suspiciousEmailDomainRule()
        )

    private fun highAmountRule(): RiskRule =
        RiskRule { event ->
            if (event.payment.amount >= riskProperties.highAmountThreshold) {
                RuleResult(
                    code = "HIGH_AMOUNT",
                    reason = "Transaction amount exceeds high-risk threshold",
                    scoreImpact = riskProperties.scoreImpacts.highAmount
                )
            } else {
                null
            }
        }

    private fun foreignBillingCountryRule(): RiskRule =
        RiskRule { event ->
            if (event.billingAddress.country.uppercase() !in riskProperties.lowRiskCountries) {
                RuleResult(
                    code = "FOREIGN_BILLING_COUNTRY",
                    reason = "Billing country is outside the low-risk country set",
                    scoreImpact = riskProperties.scoreImpacts.foreignBillingCountry
                )
            } else {
                null
            }
        }

    private fun highRiskMerchantCategoryRule(): RiskRule =
        RiskRule { event ->
            if (event.merchant.merchantCategory.lowercase() in riskProperties.highRiskMerchantCategories) {
                RuleResult(
                    code = "HIGH_RISK_MERCHANT_CATEGORY",
                    reason = "Merchant category is classified as high risk",
                    scoreImpact = riskProperties.scoreImpacts.highRiskMerchantCategory
                )
            } else {
                null
            }
        }

    private fun missingDeviceIdRule(): RiskRule =
        RiskRule { event ->
            if (event.device.deviceId.isNullOrBlank()) {
                RuleResult(
                    code = "MISSING_DEVICE_ID",
                    reason = "Device ID is missing",
                    scoreImpact = riskProperties.scoreImpacts.missingDeviceId
                )
            } else {
                null
            }
        }

    private fun suspiciousEmailDomainRule(): RiskRule =
        RiskRule { event ->
            val domain = event.customer.email.substringAfter("@", "").lowercase()

            if (domain.isBlank()) {
                RuleResult(
                    code = "INVALID_EMAIL_DOMAIN",
                    reason = "Customer email domain is invalid or missing",
                    scoreImpact = riskProperties.scoreImpacts.suspiciousEmailDomain
                )
            } else if (domain in riskProperties.suspiciousEmailDomains) {
                RuleResult(
                    code = "SUSPICIOUS_EMAIL_DOMAIN",
                    reason = "Customer email domain is in a suspicious domain list",
                    scoreImpact = riskProperties.scoreImpacts.suspiciousEmailDomain
                )
            } else {
                null
            }
        }
}