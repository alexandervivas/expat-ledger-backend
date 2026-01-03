package com.expatledger.kernel.domain

import java.util.UUID
import java.time.OffsetDateTime

trait Event:
  def id: UUID
  def aggregateId: UUID
  def occurredAt: OffsetDateTime
