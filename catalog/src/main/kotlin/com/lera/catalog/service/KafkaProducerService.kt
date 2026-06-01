package com.lera.catalog.service

import com.lera.catalog.dto.orders.GoodsInvalidateMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, GoodsInvalidateMessage>,
    @param:Value("\${spring.kafka.topics.invalidate-goods-cache}") private val invalidateGoodsCacheTopic: String
) {
    private val log = LoggerFactory.getLogger(KafkaProducerService::class.java)

    fun sendInvalidateMessage(message: GoodsInvalidateMessage) {
        kafkaTemplate.send(invalidateGoodsCacheTopic, message)
            .whenComplete { result, ex ->
                if (ex != null) {
                    log.error("Failed to send goods invalidation message: {}", ex.message)
                } else {
                    log.debug("Goods invalidation sent, offset = {}", result.recordMetadata.offset())
                }
            }
        kafkaTemplate.flush()
    }
}
