package com.lera.catalog.validator

import com.lera.catalog.util.StockInvalidateException
import org.springframework.stereotype.Component

@Component
class CatalogValidator {

    fun validateStock(stock: Int) {
        if (stock < 0) {
            throw StockInvalidateException()
        }
    }
}