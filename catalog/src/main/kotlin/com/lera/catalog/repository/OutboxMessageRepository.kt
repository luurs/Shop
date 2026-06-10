package com.lera.catalog.repository

import com.lera.catalog.model.OutboxMessageEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface OutboxMessageRepository : CrudRepository<OutboxMessageEntity, Long> {

    @Query("select * from outbox_message where status = 'NEW' order by created_at limit :limit")
    fun findNew(limit: Int): List<OutboxMessageEntity>
}