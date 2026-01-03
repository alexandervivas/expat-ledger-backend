package com.expatledger.kernel.application

import com.expatledger.kernel.domain.OutboxEvent

trait EventPublisher[F[_]]:
  def publish(event: OutboxEvent): F[Unit]
