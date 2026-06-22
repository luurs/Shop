package com.lera.catalog.repository

import com.lera.catalog.model.GoodEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.ListPagingAndSortingRepository

interface GoodRepository :
    ListCrudRepository<GoodEntity, Long>,
    ListPagingAndSortingRepository<GoodEntity, Long> {

    @Query("SELECT * FROM good WHERE external_id = :externalId AND deleted = 'false'")
    fun findByExternalId(externalId: String): GoodEntity?

    @Query("SELECT * FROM good WHERE external_id IN (:externalIds) AND deleted = 'false'")
    fun findByExternalIdIn(externalIds: List<String>): List<GoodEntity>

    @Query("SELECT * FROM good WHERE deleted = :deleted")
    fun findAllByDeleted(deleted: Boolean, pageable: Pageable): Page<GoodEntity>
}
