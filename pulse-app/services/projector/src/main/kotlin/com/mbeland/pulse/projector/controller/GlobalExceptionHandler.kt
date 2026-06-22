package com.mbeland.pulse.projector.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Validation Failed"
        detail.setProperty(
            "errors",
            ex.bindingResult.fieldErrors.map { error ->
                mapOf("field" to error.field, "message" to (error.defaultMessage ?: "invalid"))
            },
        )
        return detail
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedError(ex: Exception): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        detail.title = "Internal Server Error"
        detail.detail = "An unexpected error occurred."
        return detail
    }
}
