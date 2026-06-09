package com.lera.catalog.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lera.catalog.dto.orders.GoodsInvalidateMessage
import com.lera.catalog.dto.orders.GoodsItem
import com.lera.catalog.model.GoodEntity
import com.lera.catalog.model.OutboxMessageEntity
import com.lera.catalog.repository.GoodRepository
import com.lera.catalog.repository.OutboxMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class GoodService(
    private val goodRepository: GoodRepository,
    private val outboxMessageRepository: OutboxMessageRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun add(name: String, description: String, price: BigDecimal, externalId: String): GoodEntity {
        val result = goodRepository.findByExternalId(externalId)?.let { existing ->
            goodRepository.save(
                existing.copy(
                    name = name,
                    description = description,
                    price = price
                )
            )
        } ?: goodRepository.save(
            GoodEntity(
                name = name,
                description = description,
                price = price,
                externalId = externalId
            )
        )

        val message = GoodsInvalidateMessage(listOf(GoodsItem(result.id!!, result.externalId)))

        outboxMessageRepository.save(
            OutboxMessageEntity(
                eventType = "GOODS_INVALIDATE",
                payload = objectMapper.writeValueAsString(message)
            )
        )

        return result
    }

    fun findByExternalId(externalIds: List<String>): List<GoodEntity> =
        goodRepository.findByExternalIdIn(externalIds)
}
