package com.expatledger.tenants.persistence

import cats.syntax.all.*
import com.expatledger.kernel.domain.OutboxEvent
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import cats.effect.MonadCancelThrow

trait OutboxRepository[F[_]]:
  def save(event: OutboxEvent): Command[OutboxEvent]
  def saveAll(events: List[OutboxEvent]): F[Unit]

object SkunkOutboxRepository:
  val encoder: Encoder[OutboxEvent] =
    (uuid *: text *: uuid *: text *: text *: timestamptz).contramap { e =>
      e.id *: e.aggregateType *: e.aggregateId *: e.eventType *: e.payload *: e.occurredAt *: EmptyTuple
    }

  val insert: Command[OutboxEvent] =
    sql"""
      INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload, occurred_at)
      VALUES ($encoder)
    """.command

class SkunkOutboxRepository[F[_]: MonadCancelThrow](session: Session[F]) extends OutboxRepository[F]:
  import SkunkOutboxRepository.*

  override def save(event: OutboxEvent): Command[OutboxEvent] = insert

  override def saveAll(events: List[OutboxEvent]): F[Unit] =
    session.prepare(insert).flatMap(pc => events.traverse_(pc.execute))
