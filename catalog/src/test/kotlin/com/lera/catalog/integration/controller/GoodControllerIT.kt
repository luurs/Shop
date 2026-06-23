package com.lera.catalog.integration.controller

import com.lera.catalog.dto.goodController.CreateGoodRequest
import com.lera.catalog.dto.goodController.GetGoodResponse
import com.lera.catalog.dto.goodController.GetGoodsListRequest
import com.lera.catalog.dto.goodController.GetGoodsListResponse
import com.lera.catalog.dto.goodController.UpdateGoodRequest
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

    @Test
    @DisplayName("Проверка получения одного товара")
    fun getOneGoodTest() {
        insertGood("pizza", "tasty pizza", BigDecimal.valueOf(100), "P123ZA")

        val response = given()
            .contentType(ContentType.JSON)
            .pathParam("externalId", "P123ZA")
            .`when`()
            .get("/goods/{externalId}")
            .then()
            .statusCode(200)
            .extract()
            .`as`(GetGoodResponse::class.java)

        assertThat(response.name).isEqualTo("pizza")
        assertThat(response.description).isEqualTo("tasty pizza")
        assertThat(response.price).isEqualTo(BigDecimal("100.00"))
        assertThat(response.externalId).isEqualTo("P123ZA")
    }

    @Test
    @DisplayName("Просим товар но он не найден -> 404")
    fun getGoodNotFoundTest() {
        insertGood("pizza", "tasty pizza", BigDecimal.valueOf(100), "P123ZA")

        given()
            .contentType(ContentType.JSON)
            .pathParam("externalId", "PooPA")
            .`when`()
            .get("/goods/{externalId}")
            .then()
            .statusCode(404)
            .body("message", equalTo("Good with externalId=PooPA not found"))
    }

    @Test
    @DisplayName("Успешное обновление существующего товара")
    fun updateGoodSuccess() {
        insertGood("pizza", "tasty pizza", BigDecimal.valueOf(100), "P123ZA")

        given()
            .contentType(ContentType.JSON)
            .pathParam("externalId", "P123ZA")
            .body(
                UpdateGoodRequest(
                    "pizza2",
                    "very tasty pizza",
                    BigDecimal.valueOf(111)
                )
            )
            .`when`()
            .put("/goods/{externalId}")
            .then()
            .statusCode(200)

        val result = findAllGoods().first()
        assertThat(result.name).isEqualTo("pizza2")
        assertThat(result.description).isEqualTo("very tasty pizza")
        assertThat(result.price).isEqualTo(BigDecimal("111.00"))
        assertThat(result.externalId).isEqualTo("P123ZA")

        val outbox = outboxMessageRepository.findAll().toList()
        assertThat(outbox).hasSize(1)
    }

    @Test
    @DisplayName("Успешное софт удаление товара: смена флага delete на true")
    fun softDeleteGoodSuccess() {
        insertGood("pizza", "tasty pizza", BigDecimal.valueOf(100), "P123ZA")

        given()
            .contentType(ContentType.JSON)
            .pathParam("externalId", "P123ZA")
            .`when`()
            .delete("/goods/{externalId}")
            .then()
            .statusCode(200)

        val result = findAllGoods().first()
        assertThat(result.deleted).isTrue

        val outbox = outboxMessageRepository.findAll().toList()
        assertThat(outbox).hasSize(1)
    }

    @Test
    @DisplayName("Пагинация - две страницы по 2 товара максимум")
    fun getAllGoodsWithPagination() {
        insertGood("pizza", "tasty pizza", BigDecimal.valueOf(100), "P123ZA")
        insertGood("apple", "tasty apple", BigDecimal.valueOf(50), "test1")
        insertGood("banana", "tasty banana", BigDecimal.valueOf(150), "test2")

        given()
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 2)
            .`when`()
            .get("/goods")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
            .body("totalElements", equalTo(3))
            .body("totalPages", equalTo(2))
    }

    @Test
    @DisplayName("Сортировка по цене")
    fun getAllGoodsSortedByPrice() {
        insertGood("pizza", "tasty pizza", BigDecimal.valueOf(100), "P123ZA")
        insertGood("apple", "tasty apple", BigDecimal.valueOf(50), "test1")
        insertGood("banana", "tasty banana", BigDecimal.valueOf(150), "test2")

        given()
            .contentType(ContentType.JSON)
            .queryParam("sort", "price,asc")
            .`when`()
            .get("/goods")
            .then()
            .statusCode(200)
            .body("content[0].externalId", equalTo("test1"))
            .body("content[1].externalId", equalTo("P123ZA"))
            .body("content[2].externalId", equalTo("test2"))
    }

    @Test
    @DisplayName("Вернется список только удаленных товаров")
    fun getAllGoodsOnlyDeleted() {
        insertGood("pizza", "tasty pizza", BigDecimal.valueOf(100), "P123ZA")
        insertGood("apple", "tasty apple", BigDecimal.valueOf(50), "test1")
        insertGood("banana", "tasty banana", BigDecimal.valueOf(150), "test2")
        jdbcTemplate.update("UPDATE good SET deleted = true WHERE external_id IN ('P123ZA', 'test1')")

        given()
            .contentType(ContentType.JSON)
            .queryParam("deleted", true)
            .`when`()
            .get("/goods")
            .then()
            .statusCode(200)
            .body("totalElements", equalTo(2))
            .body("content[0].externalId", equalTo("P123ZA"))
            .body("content[1].externalId", equalTo("test1"))
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
