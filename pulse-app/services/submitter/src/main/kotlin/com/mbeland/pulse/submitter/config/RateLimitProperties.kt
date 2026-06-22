package com.mbeland.pulse.submitter.config

import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "pulse.rate-limit")
data class RateLimitProperties(
    @field:Min(1) val capacity: Long = 20,
    @field:Min(1) val refillTokens: Long = 20,
    @field:Min(1) val refillSeconds: Long = 60
)
