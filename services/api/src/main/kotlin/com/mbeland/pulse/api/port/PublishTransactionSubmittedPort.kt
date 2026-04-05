package com.mbeland.pulse.api.port

import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent

interface PublishTransactionSubmittedPort {
    fun publish(event: TransactionSubmittedEvent)
}
