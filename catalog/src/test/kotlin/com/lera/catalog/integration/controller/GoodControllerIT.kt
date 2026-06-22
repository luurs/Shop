package com.lera.catalog.integration.controller

import com.lera.catalog.dto.goodController.CreateGoodRequest
import com.lera.catalog.dto.goodController.GetGoodsListRequest
import com.lera.catalog.dto.goodController.GetGoodsListResponse
import com.lera.catalog.integration.BaseIntegrationTest
import com.lera.catalog.model.EventType
import com.lera.catalog.model.GoodEntity
import com.lera.catalog.model.OutboxMessageStatus
import com.lera.catalog.repository.OutboxMessageRepository
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.DataClassRowMapper
import java.math.BigDecimal

class GoodControllerIT : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxMessageRepository: OutboxMessageRepository

    @Test
    @DisplayName("Проверка создания товара + запись в кафку")
    fun createGoodTest() {
        given()
            .contentType(ContentType.JSON)
            .body(CreateGoodRequest("pizza", "tasty pizza", BigDecimal.valueOf(100), "P123ZA"))
            .`when`()
            .post("/goods/createGood")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))

        val good = findAllGoods().first()
        assertThat(good.externalId).isEqualTo("P123ZA")
        assertThat(good.id).isEqualTo(1L)
        assertThat(good.name).isEqualTo("pizza")
        assertThat(good.description).isEqualTo("tasty pizza")
        assertThat(good.price).isEqualTo(BigDecimal("100.00"))

        val outbox = outboxMessageRepository.findAll().toList()
        assertThat(outbox).hasSize(1)
        val row = outbox.first()
        assertThat(row.status).isEqualTo(OutboxMessageStatus.NEW)
        assertThat(row.eventType).isEqualTo(EventType.GOODS_INVALIDATE)
        assertThat(row.payload).contains("\"id\":1")
        assertThat(row.payload).contains("\"externalId\":\"P123ZA\"")
    }

    @Test
    @DisplayName("Проверка возврата списка товаров по списку externalIds")
    fun getGoodsListTest() {
        insertGood("pizza", "tasty pizza", 100, "P123ZA")
        insertGood("apple", "green apple", 15, "A33LE")

        val response = given()
            .contentType(ContentType.JSON)
            .body(GetGoodsListRequest(listOf("P123ZA", "A33LE")))
            .`when`()
            .post("/goods/getGoodsList")
            .then()
            .statusCode(200)
            .extract()
            .`as`(GetGoodsListResponse::class.java)

        assertThat(response.goods.size).isEqualTo(2)
        val good1 = response.goods[0]
        val good2 = response.goods[1]

        assertThat(good1.id).isEqualTo(1L)
        assertThat(good1.name).isEqualTo("pizza")
        assertThat(good1.description).isEqualTo("tasty pizza")
        assertThat(good1.price).isEqualTo(BigDecimal("100.00"))
        assertThat(good1.externalId).isEqualTo("P123ZA")

        assertThat(good2.id).isEqualTo(2L)
        assertThat(good2.name).isEqualTo("apple")
        assertThat(good2.description).isEqualTo("green apple")
        assertThat(good2.price).isEqualTo(BigDecimal("15.00"))
        assertThat(good2.externalId).isEqualTo("A33LE")
    }

    @Test
    @DisplayName("Проверка если товар есть в БД, его данные обновляются, а не создается новый товар")
    fun createGoodWhenAlreadyExists() {
        insertGood("pizza", "tasty pizza", 100, "P123ZA")

        given()
            .contentType(ContentType.JSON)
            .body(CreateGoodRequest("pizza2.0", "very tasty pizza", BigDecimal.valueOf(115), "P123ZA"))
            .post("/goods/createGood")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))

        val good = findAllGoods().first()

        assertThat(good.id).isEqualTo(1L)
        assertThat(good.name).isEqualTo("pizza2.0")
        assertThat(good.description).isEqualTo("very tasty pizza")
        assertThat(good.price).isEqualTo(BigDecimal("115.00"))
        assertThat(good.externalId).isEqualTo("P123ZA")
    }

    private fun insertGood(name: String, description: String, price: Number, externalId: String) {
        jdbcTemplate.update(
            "INSERT INTO good (name, description, price, external_id) VALUES (?, ?, ?, ?)",
            name, description, price, externalId
        )
    }

    private fun findAllGoods(): List<GoodEntity> =
        jdbcTemplate.query("SELECT * FROM good", DataClassRowMapper(GoodEntity::class.java))

}
