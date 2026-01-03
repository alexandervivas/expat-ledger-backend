package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import cats.syntax.all.*
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import com.expatledger.kernel.domain.{OutboxEvent, OutboxRepository}
import fs2.Stream


private object SkunkOutboxRepository:
  private val decoder: Decoder[OutboxEvent] =
    (uuid *: text *: uuid *: text *: text *: timestamptz).map {
      case id *: aggregateType *: aggregateId *: eventType *: payload *: occurredAt *: EmptyTuple =>
        OutboxEvent(id, aggregateType, aggregateId, eventType, payload, occurredAt)
    }

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

  private val selectUnprocessed: Query[Int, OutboxEvent] =
    sql"""
      SELECT id, aggregate_type, aggregate_id, event_type, payload, occurred_at
      FROM outbox
      WHERE processed_at IS NULL
      ORDER BY occurred_at ASC
      LIMIT $int4
    """.query(decoder)

  private def updateProcessed(n: Int): Command[List[java.util.UUID]] =
    sql"""
      UPDATE outbox
      SET processed_at = NOW()
      WHERE id IN (${uuid.list(n)})
    """.command

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

  override def fetchUnprocessed(limit: Int): F[List[OutboxEvent]] =
    session.prepare(selectUnprocessed).flatMap(_.stream(limit, 1024).compile.toList)

  override def markProcessed(ids: List[java.util.UUID]): F[Unit] =
    if ids.isEmpty then Sync[F].unit
    else session.prepare(updateProcessed(ids.length)).flatMap(_.execute(ids)).void
