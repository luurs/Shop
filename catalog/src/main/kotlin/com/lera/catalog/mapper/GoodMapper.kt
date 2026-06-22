package com.lera.catalog.mapper

import com.lera.catalog.dto.goodController.GetGoodResponse
import com.lera.catalog.dto.goodController.GetGoodsListResponse
import com.lera.catalog.model.GoodEntity
import org.springframework.stereotype.Component

@Component
class GoodMapper {

    fun toGoodDtoList(goodEntities: List<GoodEntity>): GetGoodsListResponse =
        GetGoodsListResponse(goodEntities.map { toGoodDto(it) })

    fun toGoodDto(goodEntity: GoodEntity): GetGoodsListResponse.GoodDto =
        GetGoodsListResponse.GoodDto(
            goodEntity.id!!,
            goodEntity.name,
            goodEntity.description,
            goodEntity.price,
            goodEntity.externalId
        )

    fun fromEntityToGoodDto(goodEntity: GoodEntity): GetGoodResponse =
        GetGoodResponse(
            goodEntity.name,
            goodEntity.description,
            goodEntity.price,
            goodEntity.externalId
        )
}
