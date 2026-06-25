package com.lera.catalog.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lera.catalog.dto.goodController.GetGoodResponse
import com.lera.catalog.dto.goodController.GetGoodsListResponse
import com.lera.catalog.dto.goodController.PageableGetGoodsListResponse
import com.lera.catalog.dto.orders.GoodsInvalidateMessage
import com.lera.catalog.dto.orders.GoodsItem
import com.lera.catalog.mapper.GoodMapper
import com.lera.catalog.model.EventType
import com.lera.catalog.model.GoodEntity
import com.lera.catalog.model.OutboxMessageEntity
import com.lera.catalog.repository.GoodRepository
import com.lera.catalog.repository.OutboxMessageRepository
import com.lera.catalog.util.GoodNotFoundException
import com.lera.catalog.util.StockInvalidateException
import com.lera.catalog.validator.CatalogValidator
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class GoodService(
    private val goodRepository: GoodRepository,
    private val outboxMessageRepository: OutboxMessageRepository,
    private val objectMapper: ObjectMapper,
    private val goodMapper: GoodMapper,
    private val catalogValidator: CatalogValidator
) {

    @Transactional
    fun add(name: String, description: String, price: BigDecimal, externalId: String, stock: Int): GoodEntity {
        catalogValidator.validateStock(stock)

        val result = goodRepository.findByExternalId(externalId)?.let { existing ->
            goodRepository.save(
                existing.copy(
                    name = name,
                    description = description,
                    price = price,
                    stock = existing.stock + stock
                )
            )
        } ?: goodRepository.save(
            GoodEntity(
                name = name,
                description = description,
                price = price,
                externalId = externalId,
                stock = stock
            )
        )

        saveOutboxMessage(result)

        return result
    }

    fun findByExternalId(externalIds: List<String>): GetGoodsListResponse {
        val goodsEntities = goodRepository.findByExternalIdIn(externalIds)
        return goodMapper.toGoodDtoList(goodsEntities)
    }

    fun findByExternalId(externalId: String): GetGoodResponse {
        val goodEntity = goodRepository.findByExternalId(externalId)
            ?: throw GoodNotFoundException(externalId)
        return goodMapper.fromEntityToGoodDto(goodEntity)
    }

    @Transactional
    fun update(externalId: String, name: String, description: String, price: BigDecimal, stock: Int) {
        catalogValidator.validateStock(stock)

        val updated = goodRepository.findByExternalId(externalId)?.let { existing ->
            val updated = existing.copy(
                name = name,
                description = description,
                price = price,
                stock = stock
            )
            goodRepository.save(updated)
        } ?: throw GoodNotFoundException(externalId)

        saveOutboxMessage(updated)
    }

    @Transactional
    fun delete(externalId: String) {
        val deletedGood = goodRepository.findByExternalId(externalId)?.let { existing ->
            goodRepository.save(
                existing.copy(
                    deleted = true
                )
            )
        } ?: throw GoodNotFoundException(externalId)

        saveOutboxMessage(deletedGood)
    }

    private fun saveOutboxMessage(good: GoodEntity) {
        val message = GoodsInvalidateMessage(listOf(GoodsItem(good.id!!, good.externalId)))
        outboxMessageRepository.save(
            OutboxMessageEntity(
                eventType = EventType.GOODS_INVALIDATE,
                payload = objectMapper.writeValueAsString(message)
            )
        )
    }

    fun getGoods(pageable : Pageable, deleted: Boolean): PageableGetGoodsListResponse {
        val page = goodRepository.findAllByDeleted(deleted, pageable)
        return PageableGetGoodsListResponse(
            content = page.content.map { goodMapper.fromEntityToGoodDto(it) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number
        )
    }
}
