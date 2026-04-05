package com.mbeland.pulse.projector.repository

import com.mbeland.pulse.projector.domain.TransactionRiskProjection
import com.mbeland.pulse.projector.port.FindTransactionProjectionPort
import com.mbeland.pulse.projector.port.SaveTransactionProjectionPort
import org.springframework.data.repository.CrudRepository

interface TransactionRiskProjectionRepository :
    CrudRepository<TransactionRiskProjection, Long>,
    SaveTransactionProjectionPort,
    FindTransactionProjectionPort
