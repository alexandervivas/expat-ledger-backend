package com.expatledger.kernel.domain

import java.util.UUID
import java.time.OffsetDateTime

trait Event:
  def id: UUID
  def aggregateId: UUID
  def occurredAt: OffsetDateTime

trait DomainEvent extends Event:
  def eventType: String

case class OutboxEvent(
    override val id: UUID,
    aggregateType: String,
    override val aggregateId: UUID,
    eventType: String,
    payload: String, // Serialized JSON
    override val occurredAt: OffsetDateTime
) extends Event
