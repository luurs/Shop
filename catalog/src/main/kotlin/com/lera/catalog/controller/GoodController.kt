package com.lera.catalog.controller

import com.lera.catalog.dto.goodController.CreateGoodRequest
import com.lera.catalog.dto.goodController.CreateGoodResponse
import com.lera.catalog.dto.goodController.GetGoodResponse
import com.lera.catalog.dto.goodController.GetGoodsListRequest
import com.lera.catalog.dto.goodController.GetGoodsListResponse
import com.lera.catalog.dto.goodController.PageableGetGoodsListResponse
import com.lera.catalog.dto.goodController.UpdateGoodRequest
import com.lera.catalog.service.GoodService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/goods")
class GoodController(
    val goodService: GoodService
) {

    @PostMapping("/createGood")
    fun addGood(@RequestBody request: CreateGoodRequest): CreateGoodResponse {
        val addedGood = goodService.add(request.name, request.description, request.price, request.externalId)
        return CreateGoodResponse(addedGood.id!!)
    }

    @PostMapping("/getGoodsList")
    fun getGoodsList(@RequestBody request: GetGoodsListRequest): GetGoodsListResponse {
        return goodService.findByExternalId(request.externalIds)
    }

    @GetMapping("/{externalId}")
    fun getGood(@PathVariable externalId: String): GetGoodResponse {
        return goodService.findByExternalId(externalId)
    }

    @PutMapping("/{externalId}")
    fun updateGood(@PathVariable externalId: String, @RequestBody request: UpdateGoodRequest) {
        goodService.update(externalId, request.name, request.description, request.price)
    }

    @DeleteMapping("/{externalId}")
    fun softDeleteGood(@PathVariable externalId: String) {
        goodService.delete(externalId)
    }

    @GetMapping
    fun getAllGoodsList(
        pageable: Pageable,
        @RequestParam(required = false, defaultValue = "false") deleted: Boolean
    ): PageableGetGoodsListResponse {
        return goodService.getGoods(pageable, deleted)
    }
}
