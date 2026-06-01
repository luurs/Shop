package com.lera.catalog.dto

import java.math.BigDecimal

data class CreateGoodRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val externalId: String
)
