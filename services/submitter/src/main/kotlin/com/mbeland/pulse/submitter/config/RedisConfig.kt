package com.mbeland.pulse.submitter.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbeland.pulse.submitter.dto.SubmitTransactionResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun idempotencyRedisTemplate(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): RedisTemplate<String, SubmitTransactionResponse> {
        val template = RedisTemplate<String, SubmitTransactionResponse>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = Jackson2JsonRedisSerializer(objectMapper, SubmitTransactionResponse::class.java)
        return template
    }
}
