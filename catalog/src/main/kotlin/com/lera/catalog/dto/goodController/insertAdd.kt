package com.lera.catalog.dto.goodController

import java.math.BigDecimal

data class CreateGoodRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val externalId: String,
    val stock: Int
)

data class CreateGoodResponse(val id: Long)