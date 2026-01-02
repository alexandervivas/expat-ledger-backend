package com.expatledger.kernel.domain

import java.util.UUID
import java.time.OffsetDateTime

trait Event:
  def id: UUID
  def aggregateId: UUID
  def occurredAt: OffsetDateTime

trait DomainEvent extends Event

case class OutboxEvent(
    id: UUID,
    aggregateType: String,
    aggregateId: UUID,
    eventType: String,
    payload: String, // Serialized JSON
    occurredAt: OffsetDateTime
) extends Event
