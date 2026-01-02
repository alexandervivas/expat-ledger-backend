package com.expatledger.kernel.domain

import java.util.UUID
import java.time.OffsetDateTime

trait DomainEvent:
  def id: UUID
  def aggregateId: UUID
  def occurredAt: OffsetDateTime

trait OutboxEvent:
  def id: UUID
  def aggregateType: String
  def aggregateId: UUID
  def eventType: String
  def payload: String // Serialized JSON
  def occurredAt: OffsetDateTime
