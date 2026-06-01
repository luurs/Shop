package com.lera.catalog.repository

import com.lera.catalog.model.GoodEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository

interface GoodRepository : ListCrudRepository<GoodEntity, Long> {

    @Query("SELECT * FROM good WHERE external_id = :externalId")
    fun findByExternalId(externalId: String): GoodEntity?

    @Query("SELECT * FROM good WHERE external_id IN (:externalIds)")
    fun findByExternalIdIn(externalIds: List<String>): List<GoodEntity>
}
