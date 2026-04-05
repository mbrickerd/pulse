package com.mbeland.pulse.api.config

import jakarta.validation.constraints.DecimalMin
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal

@Validated
@ConfigurationProperties(prefix = "pulse.risk")
data class RiskProperties(
    @field:DecimalMin("0.01")
    val manualReviewThreshold: BigDecimal
)
