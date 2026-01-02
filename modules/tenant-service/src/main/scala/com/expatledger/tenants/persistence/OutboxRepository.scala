package com.expatledger.tenants.persistence

import cats.syntax.all.*
import com.expatledger.kernel.domain.OutboxEvent
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import cats.effect.MonadCancelThrow

trait OutboxRepository[F[_]]:
  def save(event: OutboxEvent): F[Unit]

  def saveAll(events: List[OutboxEvent]): F[Unit]

private object SkunkOutboxRepository:
  private val encoder: Encoder[OutboxEvent] =
    (uuid *: text *: uuid *: text *: text *: timestamptz).values.contramap { e =>
      e.id *: e.aggregateType *: e.aggregateId *: e.eventType *: e.payload *: e.occurredAt *: EmptyTuple
    }

  private val insert: Command[OutboxEvent] =
    sql"""
      INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload, occurred_at)
      VALUES ($encoder)
    """.command

  private def insertMany(n: Int): Command[List[OutboxEvent]] = {
    val encoderMany = encoder.list(n)
    sql"""
      INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload, occurred_at)
      VALUES ($encoderMany)
    """.command
  }

class SkunkOutboxRepository[F[_] : MonadCancelThrow](session: Session[F]) extends OutboxRepository[F]:

  import SkunkOutboxRepository.*

  override def save(event: OutboxEvent): F[Unit] =
    session.execute(insert)(event).void

  override def saveAll(events: List[OutboxEvent]): F[Unit] =
    events.grouped(500).toList.groupBy(_.size).toList.traverse_ { case (size, chunks) =>
      session.prepare(insertMany(size)).flatMap { cmd =>
        chunks.traverse_(cmd.execute)
      }
    }
