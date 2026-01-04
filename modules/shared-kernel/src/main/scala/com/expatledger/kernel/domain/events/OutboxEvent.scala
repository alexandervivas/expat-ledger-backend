package com.expatledger.kernel.domain.events

import io.circe.Json

import java.time.OffsetDateTime
import java.util.UUID

case class OutboxEvent(
                        override val id: UUID,
                        aggregateType: String,
                        override val aggregateId: UUID,
                        eventType: String,
                        payload: Json,
                        avroPayload: Array[Byte],
                        schemaUrn: String,
                        override val occurredAt: OffsetDateTime
) extends Event
