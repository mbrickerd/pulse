package com.mbeland.pulse.submitter.port

import com.mbeland.pulse.model.transaction.TransactionSubmittedEvent

interface PublishTransactionSubmittedPort {
    fun publish(event: TransactionSubmittedEvent)
}
