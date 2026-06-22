package com.lera.catalog.util

import org.springframework.http.HttpStatus

data class ApiResponse(
    val message: String,
    val status: HttpStatus
) {
}