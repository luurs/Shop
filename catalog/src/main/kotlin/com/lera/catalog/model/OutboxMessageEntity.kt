package com.lera.catalog.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("outbox_message")
data class OutboxMessageEntity(
    @Id
    val id: Long? = null,
    @Column("event_type")
    val eventType: String,
    val payload: String,
    @Column("created_at")
    val createdAt: Instant = Instant.now(),
    val status: OutboxMessageStatus = OutboxMessageStatus.NEW
) {
}