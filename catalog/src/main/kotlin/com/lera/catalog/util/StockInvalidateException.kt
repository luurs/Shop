package com.lera.catalog.util

import org.springframework.http.HttpStatus

class StockInvalidateException: ApiException(
    "Stock is less than zero",
    HttpStatus.BAD_REQUEST
) {
}