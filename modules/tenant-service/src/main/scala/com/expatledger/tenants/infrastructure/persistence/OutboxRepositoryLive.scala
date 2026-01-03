package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import cats.syntax.all.*
import com.expatledger.kernel.domain.events.OutboxEvent
import com.expatledger.kernel.domain.repositories.OutboxRepository
import skunk.*
import skunk.codec.all.*
import skunk.syntax.all.*

import java.util.UUID

private object OutboxRepositoryLive {
  private val codec: Codec[OutboxEvent] =
    (uuid *: varchar *: uuid *: varchar *: varchar *: varchar *: timestamptz *: EmptyTuple).tupled.imap {
      case id *: aggregateType *: aggregateId *: eventType *: payload *: schemaUrn *: occurredAt *: EmptyTuple =>
        OutboxEvent(id, aggregateType, aggregateId, eventType, payload, schemaUrn, occurredAt)
    }(outboxEvent => outboxEvent.id *: outboxEvent.aggregateType *: outboxEvent.aggregateId *: outboxEvent.eventType *: outboxEvent.payload *: outboxEvent.schemaUrn *: outboxEvent.occurredAt *: EmptyTuple)

  private val insert: Command[OutboxEvent] =
    sql"""
         INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload, schema_urn, occurred_at)
         VALUES $codec
       """.command

  private val selectUnprocessed: Query[Int, OutboxEvent] =
    sql"""
         SELECT id, aggregate_type, aggregate_id, event_type, payload, schema_urn, occurred_at
         FROM outbox
         WHERE processed_at IS NULL
         ORDER BY occurred_at ASC
         LIMIT $int4
       """.query(codec)

  private def updateProcessed(size: Int): Command[List[UUID]] =
    sql"""
         UPDATE outbox
         SET processed_at = NOW()
         WHERE id IN (${uuid.list(size)})
       """.command

}

class OutboxRepositoryLive[F[_] : Sync](pool: Resource[F, Session[F]]) extends OutboxRepository[F] {

  import OutboxRepositoryLive.*

  override def save(event: OutboxEvent): F[Unit] =
    pool.use { session =>
      for {
        command <- session.prepare(insert)
        _ <- command.execute(event)
      } yield ()
    }

  override def fetchUnprocessed(limit: Int): F[List[OutboxEvent]] =
    pool.use(_.prepare(selectUnprocessed).flatMap(_.stream(limit, 1024).compile.toList))

  override def markProcessed(ids: List[java.util.UUID]): F[Unit] = {
    pool.use { session =>
      if ids.isEmpty then Sync[F].unit
      else session.prepare(updateProcessed(ids.length)).flatMap(_.execute(ids)).void
    }

  }

}
