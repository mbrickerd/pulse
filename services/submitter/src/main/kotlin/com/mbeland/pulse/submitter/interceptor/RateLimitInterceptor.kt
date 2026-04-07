package com.mbeland.pulse.submitter.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbeland.pulse.submitter.config.RateLimitProperties
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class RateLimitInterceptor(
    private val rateLimitProperties: RateLimitProperties,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {

    private val bucketCache: LoadingCache<String, Bucket> = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build { _ -> createBucket() }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val key = SecurityContextHolder.getContext().authentication?.name ?: request.remoteAddr
        val bucket = bucketCache.get(key)

        if (bucket.tryConsume(1)) {
            return true
        }

        val detail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS)
        detail.title = "Too Many Requests"
        detail.detail = "Rate limit exceeded. Please retry after a moment."

        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        objectMapper.writeValue(response.writer, detail)
        return false
    }

    private fun createBucket(): Bucket = Bucket.builder()
        .addLimit(
            Bandwidth.classic(
                rateLimitProperties.capacity,
                Refill.greedy(rateLimitProperties.refillTokens, Duration.ofSeconds(rateLimitProperties.refillSeconds))
            )
        )
        .build()
}
