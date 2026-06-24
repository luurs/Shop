package com.lera.catalog.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("good")
data class GoodEntity(
    val name: String,
    val description: String,
    val price: BigDecimal,
    @Column("external_id")
    val externalId: String,
    @Id
    val id: Long? = null,
    val deleted: Boolean = false
)
