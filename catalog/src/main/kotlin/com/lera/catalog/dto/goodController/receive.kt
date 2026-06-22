package com.lera.catalog.dto.goodController

import java.math.BigDecimal

data class GetGoodResponse(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val externalId: String
) {}