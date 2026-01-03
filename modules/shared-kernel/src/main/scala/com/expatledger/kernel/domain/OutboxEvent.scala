package com.expatledger.kernel.domain

import java.util.UUID
import java.time.OffsetDateTime

case class OutboxEvent(
    override val id: UUID,
    aggregateType: String,
    override val aggregateId: UUID,
    eventType: String,
    payload: String, // Serialized JSON
    override val occurredAt: OffsetDateTime
) extends Event
