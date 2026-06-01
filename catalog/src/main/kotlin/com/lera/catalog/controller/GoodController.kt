package com.lera.catalog.controller

import com.lera.catalog.dto.CreateGoodRequest
import com.lera.catalog.dto.CreateGoodResponse
import com.lera.catalog.dto.GetGoodsListRequest
import com.lera.catalog.dto.GetGoodsListResponse
import com.lera.catalog.mapper.GoodMapper
import com.lera.catalog.service.GoodService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/goods")
class GoodController(
    val goodService: GoodService,
    val goodMapper: GoodMapper
) {

    @PostMapping("/createGood")
    fun addGood(@RequestBody request: CreateGoodRequest): CreateGoodResponse {
        val addedGood = goodService.add(request.name, request.description, request.price, request.externalId)
        return CreateGoodResponse(addedGood.id!!)
    }

    @PostMapping("/getGoodsList")
    fun getGoodsList(@RequestBody request: GetGoodsListRequest): GetGoodsListResponse {
        val goods = goodService.findByExternalId(request.externalIds)
        return goodMapper.toGoodDtoList(goods)
    }
}
