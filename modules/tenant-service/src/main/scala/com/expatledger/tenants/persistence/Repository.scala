package com.expatledger.tenants.persistence

import cats.effect.MonadCancelThrow
import com.expatledger.kernel.domain.{DomainEvent, OutboxEvent}
import skunk.Session
import java.util.UUID
import java.time.OffsetDateTime

trait Repository[F[_]]:
  def session: Session[F]
  
  def atomic[A](action: Session[F] => F[A])(using F: MonadCancelThrow[F]): F[A] =
    session.transaction.use(_ => action(session))

  protected def toOutboxEvent(event: DomainEvent, aggType: String, eventPayload: String): OutboxEvent =
    new OutboxEvent:
      val id: UUID = event.id
      val aggregateType: String = aggType
      val aggregateId: UUID = event.aggregateId
      val eventType: String = event.getClass.getSimpleName.replace("$", "")
      val payload: String = eventPayload
      val occurredAt: OffsetDateTime = event.occurredAt
