package com.lera.catalog.dto.goodController

import java.math.BigDecimal

data class UpdateGoodRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val stock: Int
)