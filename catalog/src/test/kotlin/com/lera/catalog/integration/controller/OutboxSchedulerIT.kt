package com.lera.catalog.integration.controller

import com.lera.catalog.dto.CreateGoodRequest
import com.lera.catalog.integration.BaseIntegrationTest
import com.lera.catalog.model.OutboxMessageStatus
import com.lera.catalog.repository.OutboxMessageRepository
import com.lera.catalog.scheduler.OutboxScheduler
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Duration

class OutboxSchedulerIT : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxScheduler: OutboxScheduler
    @Autowired
    lateinit var outboxMessageRepository: OutboxMessageRepository

    @Test
    @DisplayName("шедулер публикует new сообщение в кафку и после этого помечает как sent")
    fun publishesAndMarksSent() {
        given().contentType(ContentType.JSON)
            .body(CreateGoodRequest("pizza", "tasty pizza", BigDecimal.valueOf(100), "P122A"))
            .post("/goods/createGood")
            .then()
            .statusCode(200)

        consumerAtTopicEnd("catalog.invalidate-goods-cache").use { consumer ->
            outboxScheduler.publishOutbox()

            val records = consumer.poll(Duration.ofSeconds(5))
            assertThat(records.count()).isEqualTo(1)
            assertThat(records.iterator().next().value()).contains("\"externalId\":\"P122A\"")
        }

        val row = outboxMessageRepository.findAll().first()
        assertThat(row.status).isEqualTo(OutboxMessageStatus.SENT)
    }

    @Test
    @DisplayName("отправленные sent сообщения повторно не публикуются")
    fun doesNotResendSent() {
        given().contentType(ContentType.JSON)
            .body(CreateGoodRequest("pizza", "tasty pizza", BigDecimal.valueOf(100), "P122A"))
            .post("/goods/createGood").then().statusCode(200)

        consumerAtTopicEnd("catalog.invalidate-goods-cache").use { consumer ->
            outboxScheduler.publishOutbox()
            outboxScheduler.publishOutbox()

            val records = consumer.poll(Duration.ofSeconds(5))
            assertThat(records.count()).isEqualTo(1)
        }
    }
}