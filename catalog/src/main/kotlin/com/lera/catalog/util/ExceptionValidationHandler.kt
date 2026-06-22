package com.lera.catalog.util

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionValidationHandler {

    @ExceptionHandler
    fun handleException(e: ApiException): ResponseEntity<ApiResponse> {
        return ResponseEntity
            .status(e.status)
            .body(ApiResponse(e.message, e.status));
    }
}
