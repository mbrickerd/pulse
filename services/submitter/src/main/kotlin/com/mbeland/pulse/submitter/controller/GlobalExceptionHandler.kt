package com.mbeland.pulse.submitter.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.concurrent.ExecutionException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Validation Failed"
        detail.setProperty("errors", ex.bindingResult.fieldErrors.map { error ->
            mapOf("field" to error.field, "message" to (error.defaultMessage ?: "invalid"))
        })
        return detail
    }

    @ExceptionHandler(ExecutionException::class)
    fun handleKafkaPublishError(ex: ExecutionException): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE)
        detail.title = "Service Unavailable"
        detail.detail = "Transaction could not be submitted. Please retry."
        return detail
    }

    @ExceptionHandler(InterruptedException::class)
    fun handleInterrupted(ex: InterruptedException): ProblemDetail {
        Thread.currentThread().interrupt()
        val detail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE)
        detail.title = "Service Unavailable"
        detail.detail = "Transaction could not be submitted. Please retry."
        return detail
    }
}
