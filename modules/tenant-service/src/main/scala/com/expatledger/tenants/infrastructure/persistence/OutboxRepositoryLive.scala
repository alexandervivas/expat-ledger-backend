package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import cats.syntax.all.*
import com.expatledger.kernel.domain.events.OutboxEvent
import com.expatledger.kernel.domain.repositories.OutboxRepository
import io.circe.Json
import io.circe.parser.parse
import skunk.*
import skunk.codec.all.*
import skunk.data.Type
import skunk.syntax.all.*

import java.util.UUID

object OutboxRepositoryLive {

  private val jsonb: Codec[Json] =
    Codec.simple(
      _.noSpaces, // Encoder: Json -> String
      s => parse(s).leftMap(_.show), // Decoder: String -> Either[String, Json]
      Type.jsonb // Postgres Type OID
    )

  private val codec: Codec[OutboxEvent] =
    (uuid *: text *: uuid *: text *: jsonb *: bytea *: text *: timestamptz *: EmptyTuple).tupled.imap {
      case id *: aggregateType *: aggregateId *: eventType *: payload *: avroPayload *: schemaUrn *: occurredAt *: EmptyTuple =>
        OutboxEvent(id, aggregateType, aggregateId, eventType, payload, avroPayload, schemaUrn, occurredAt)
    }(outboxEvent => outboxEvent.id *: outboxEvent.aggregateType *: outboxEvent.aggregateId *: outboxEvent.eventType *: outboxEvent.payload *: outboxEvent.avroPayload *: outboxEvent.schemaUrn *: outboxEvent.occurredAt *: EmptyTuple)

  private val insert: Command[OutboxEvent] =
    sql"""
         INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload, avro_payload, schema_urn, occurred_at)
         VALUES ($uuid, $text, $uuid, $text, $jsonb, $bytea, $text, $timestamptz)
       """.command.contramap { e =>
      e.id *: e.aggregateType *: e.aggregateId *: e.eventType *: e.payload *: e.avroPayload *: e.schemaUrn *: e.occurredAt *: EmptyTuple
    }

  private val selectUnprocessed: Query[Int, OutboxEvent] =
    sql"""
         SELECT id, aggregate_type, aggregate_id, event_type, payload, avro_payload, schema_urn, occurred_at
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

  def make[F[_] : Sync](pool: Resource[F, Session[F]]): OutboxRepositoryLive[F] =
    new OutboxRepositoryLive[F](pool)

}

class OutboxRepositoryLive[F[_] : Sync](pool: Resource[F, Session[F]]) extends OutboxRepository[F] {

  import OutboxRepositoryLive.*

  override def save(event: OutboxEvent): F[Unit] =
    pool.use(_.execute(insert)(event).void)

  override def fetchUnprocessed(limit: Int): F[List[OutboxEvent]] =
    pool.use(_.prepare(selectUnprocessed).flatMap(_.stream(limit, 1024).compile.toList))

  override def markProcessed(ids: List[java.util.UUID]): F[Unit] = {
    pool.use { session =>
      if ids.isEmpty then Sync[F].unit
      else session.prepare(updateProcessed(ids.length)).flatMap(_.execute(ids)).void
    }

  }

}
