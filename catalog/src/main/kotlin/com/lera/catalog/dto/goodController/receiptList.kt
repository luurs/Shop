package com.lera.catalog.dto.goodController

import java.math.BigDecimal

data class GetGoodsListRequest(val externalIds: List<String>)

data class GetGoodsListResponse(val goods: List<GoodDto>) {
    data class GoodDto(
        val id: Long,
        val name: String,
        val description: String,
        val price: BigDecimal,
        val externalId: String
    )
}