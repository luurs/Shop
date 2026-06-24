package com.lera.catalog.dto.goodController

data class PageableGetGoodsListResponse(
    val content: List<GetGoodResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)