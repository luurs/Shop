package com.lera.catalog.dto

import java.math.BigDecimal

data class GetGoodsListResponse(val goods: List<GoodDto>) {
    data class GoodDto(
        val id: Long,
        val name: String,
        val description: String,
        val price: BigDecimal,
        val externalId: String
    )
}
