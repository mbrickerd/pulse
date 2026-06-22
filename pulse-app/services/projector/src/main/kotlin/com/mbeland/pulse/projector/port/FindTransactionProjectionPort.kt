package com.mbeland.pulse.projector.port

import com.mbeland.pulse.projector.domain.TransactionRiskProjection

interface FindTransactionProjectionPort {
    fun findByTransactionId(transactionId: String): TransactionRiskProjection?
}
