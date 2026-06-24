package com.lera.catalog.util

import org.springframework.http.HttpStatus

class GoodNotFoundException(externalId: String) : ApiException(
    "Good with externalId=$externalId not found",
    HttpStatus.NOT_FOUND
)