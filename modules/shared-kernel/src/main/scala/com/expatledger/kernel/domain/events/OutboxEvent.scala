package com.expatledger.kernel.domain.events

import java.time.OffsetDateTime
import java.util.UUID

case class OutboxEvent(
    override val id: UUID,
    aggregateType: String,
    override val aggregateId: UUID,
    eventType: EventType,
    payload: String, // Serialized JSON
    schemaUrn: String,
    override val occurredAt: OffsetDateTime
) extends Event
