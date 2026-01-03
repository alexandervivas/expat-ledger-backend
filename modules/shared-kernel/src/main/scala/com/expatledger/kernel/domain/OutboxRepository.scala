package com.expatledger.kernel.domain

trait OutboxRepository[F[_]]:
  def save(event: OutboxEvent): F[Unit]
  def saveAll(events: List[OutboxEvent]): F[Unit]
