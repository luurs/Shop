package com.lera.catalog.service

import com.lera.catalog.dto.orders.GoodsInvalidateMessage
import com.lera.catalog.dto.orders.GoodsItem
import com.lera.catalog.model.GoodEntity
import com.lera.catalog.repository.GoodRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class GoodService(
    private val goodRepository: GoodRepository,
    private val kafkaProducerService: KafkaProducerService
) {

    @Transactional
    fun add(name: String, description: String, price: BigDecimal, externalId: String): GoodEntity {
        val result = goodRepository.findByExternalId(externalId)?.let { existing ->
            goodRepository.save(existing.copy(name = name, description = description, price = price))
        } ?: goodRepository.save(GoodEntity(name = name, description = description, price = price, externalId = externalId))

        val message = GoodsInvalidateMessage(listOf(GoodsItem(result.id!!, result.externalId)))
        kafkaProducerService.sendInvalidateMessage(message)

        return result
    }

    fun findByExternalId(externalIds: List<String>): List<GoodEntity> =
        goodRepository.findByExternalIdIn(externalIds)
}
