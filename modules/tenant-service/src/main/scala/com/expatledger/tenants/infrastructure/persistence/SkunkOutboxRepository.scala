package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import cats.syntax.all.*
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import com.expatledger.kernel.domain.{OutboxEvent, OutboxRepository}
import fs2.Stream


private object SkunkOutboxRepository:
  private val encoder: Encoder[OutboxEvent] =
    (uuid *: text *: uuid *: text *: text *: timestamptz).values.contramap { e =>
      e.id *: e.aggregateType *: e.aggregateId *: e.eventType *: e.payload *: e.occurredAt *: EmptyTuple
    }

  private val insert: Command[OutboxEvent] =
    sql"""
      INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload, occurred_at)
      VALUES $encoder
    """.command

  private def insertMany(n: Int): Command[List[OutboxEvent]] = {
    val encoderMany = encoder.list(n)
    sql"""
      INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload, occurred_at)
      VALUES $encoderMany
    """.command
  }

class SkunkOutboxRepository[F[_] : Sync](session: Session[F]) extends OutboxRepository[F]:

  import SkunkOutboxRepository.*

  override def save(event: OutboxEvent): F[Unit] =
    session.execute(insert)(event).void

  override def saveAll(events: List[OutboxEvent]): F[Unit] =
    Stream.fromIterator[F](events.grouped(1000), chunkSize = 1)
      .evalMap { chunk =>
        session.execute(insertMany(chunk.length))(chunk).void
      }
      .compile
      .drain
