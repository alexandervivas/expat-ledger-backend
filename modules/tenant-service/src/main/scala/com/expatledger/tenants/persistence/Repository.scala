package com.expatledger.tenants.persistence

import cats.effect.MonadCancelThrow
import com.expatledger.kernel.domain.{DomainEvent, OutboxEvent}
import skunk.Session

trait Repository[F[_]]:
  def session: Session[F]
  
  def atomic[A](action: Session[F] => F[A])(using F: MonadCancelThrow[F]): F[A] =
    session.transaction.use(_ => action(session))

  protected def toOutboxEvent(event: DomainEvent, aggType: String, eventPayload: String): OutboxEvent =
    OutboxEvent(
      id = event.id,
      aggregateType = aggType,
      aggregateId = event.aggregateId,
      eventType = event.eventType,
      payload = eventPayload,
      occurredAt = event.occurredAt
    )
