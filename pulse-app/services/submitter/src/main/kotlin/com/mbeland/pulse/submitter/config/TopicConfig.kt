package com.mbeland.pulse.submitter.config

import com.mbeland.pulse.model.Topics
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class TopicConfig {

    @Bean
    fun transactionsSubmittedTopic(): NewTopic =
        TopicBuilder.name(Topics.TRANSACTIONS_SUBMITTED).build()
}
