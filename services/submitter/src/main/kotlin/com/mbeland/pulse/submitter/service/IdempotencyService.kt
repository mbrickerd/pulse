package com.mbeland.pulse.submitter.service

import com.mbeland.pulse.submitter.dto.SubmitTransactionResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class IdempotencyService(
    private val idempotencyRedisTemplate: RedisTemplate<String, SubmitTransactionResponse>
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val TTL = Duration.ofHours(24)
        private const val KEY_PREFIX = "idempotency:"
    }

    fun get(userId: String, idempotencyKey: String): SubmitTransactionResponse? {
        val redisKey = redisKey(userId, idempotencyKey)
        return idempotencyRedisTemplate.opsForValue().get(redisKey).also { cached ->
            if (cached != null) {
                log.info("Idempotency cache hit for key={}", redisKey)
            }
        }
    }

    fun store(userId: String, idempotencyKey: String, response: SubmitTransactionResponse) {
        idempotencyRedisTemplate.opsForValue().set(redisKey(userId, idempotencyKey), response, TTL)
    }

    private fun redisKey(userId: String, idempotencyKey: String) =
        "$KEY_PREFIX$userId:$idempotencyKey"
}
