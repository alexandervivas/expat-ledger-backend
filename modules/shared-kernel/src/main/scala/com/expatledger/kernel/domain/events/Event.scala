package com.expatledger.kernel.domain.events

import java.time.OffsetDateTime
import java.util.UUID

trait Event:
  def id: UUID
  def aggregateId: UUID
  def occurredAt: OffsetDateTime
