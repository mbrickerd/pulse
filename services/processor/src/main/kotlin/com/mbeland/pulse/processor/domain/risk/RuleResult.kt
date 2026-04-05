package com.mbeland.pulse.processor.domain.risk

data class RuleResult(
    val code: String,
    val reason: String,
    val scoreImpact: Int
)