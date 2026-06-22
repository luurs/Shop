package com.lera.catalog.util

import org.springframework.http.HttpStatus

open class ApiException(
    override val message: String,
    val status: HttpStatus
): RuntimeException(message)