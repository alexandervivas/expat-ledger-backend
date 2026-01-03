package com.expatledger.kernel.domain.repositories

import com.expatledger.kernel.domain.events.OutboxEvent
import java.util.UUID

trait OutboxRepository[F[_]]:
  def save(event: OutboxEvent): F[Unit]
  def fetchUnprocessed(limit: Int): F[List[OutboxEvent]]
  def markProcessed(ids: List[UUID]): F[Unit]
