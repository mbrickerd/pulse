package com.mbeland.pulse.projector.config

import com.mbeland.pulse.model.Topics
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class TopicConfig {

    @Bean
    fun transactionsAssessedTopic(): NewTopic =
        TopicBuilder.name(Topics.TRANSACTIONS_ASSESSED).build()

    @Bean
    fun transactionsAssessedDltTopic(): NewTopic =
        TopicBuilder.name("${Topics.TRANSACTIONS_ASSESSED}.DLT").build()
}
