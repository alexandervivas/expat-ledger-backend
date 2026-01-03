package com.expatledger.kernel.domain

import java.util.UUID

trait OutboxRepository[F[_]]:
  def save(event: OutboxEvent): F[Unit]
  def saveAll(events: List[OutboxEvent]): F[Unit]
  def fetchUnprocessed(limit: Int): F[List[OutboxEvent]]
  def markProcessed(ids: List[UUID]): F[Unit]
