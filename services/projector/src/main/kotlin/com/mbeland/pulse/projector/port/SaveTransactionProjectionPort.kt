package com.mbeland.pulse.projector.port

import com.mbeland.pulse.projector.domain.TransactionRiskProjection

interface SaveTransactionProjectionPort {
    fun save(projection: TransactionRiskProjection): TransactionRiskProjection
}
