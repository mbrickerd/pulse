package com.mbeland.pulse.projector.domain

import org.springframework.data.relational.core.mapping.Table

@Table("transaction_reason")
data class TransactionReason(val reason: String)
