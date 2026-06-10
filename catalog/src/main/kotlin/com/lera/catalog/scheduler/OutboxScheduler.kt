package com.lera.catalog.scheduler

import com.fasterxml.jackson.databind.ObjectMapper
import com.lera.catalog.dto.orders.GoodsInvalidateMessage
import com.lera.catalog.model.OutboxMessageStatus
import com.lera.catalog.repository.OutboxMessageRepository
import com.lera.catalog.service.KafkaProducerService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class OutboxScheduler(
    private val outboxMessageRepository: OutboxMessageRepository,
    private val objectMapper: ObjectMapper,
    private val kafkaProducerService: KafkaProducerService,
    @param:Value("\${outbox.scheduler.batch-size}")
    private val batchSize: Int
) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedDelayString = "\${outbox.scheduler.delay:5000}")
    fun publishOutbox() {
        val batch = outboxMessageRepository.findNew(batchSize)
        if (batch.isEmpty()) return

        for (msg in batch) {
            try {
                val message = objectMapper.readValue(msg.payload, GoodsInvalidateMessage::class.java)
                kafkaProducerService.sendInvalidateMessage(message)
                outboxMessageRepository.save(msg.copy(status = OutboxMessageStatus.SENT))
            } catch(e: Exception) {
                logger.error(e) { "Message (id = ${msg.id}) could not be published" }
            }
        }
    }
}