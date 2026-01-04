package com.expatledger.tenants.infrastructure.persistence

import cats.effect.*
import com.expatledger.kernel.domain.events.OutboxEvent
import io.circe.Json

import java.time.{OffsetDateTime, ZoneOffset}
import java.time.temporal.ChronoUnit
import java.util.UUID

class OutboxRepositorySpec extends SkunkPostgresSpec {

  test("OutboxRepository should save, fetch unprocessed, and mark processed") {
    withContainers { container =>
      sessionPool(container).use { pool =>
        val repo = OutboxRepositoryLive.make[IO](pool)
        val eventId = UUID.randomUUID()
        val now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
        val event = OutboxEvent(
          id = eventId,
          aggregateType = "Tenant",
          aggregateId = UUID.randomUUID(),
          eventType = "TenantCreated",
          payload = Json.obj("name" -> Json.fromString("Test Tenant")),
          avroPayload = Array(1, 2, 3),
          schemaUrn = "urn:schema",
          occurredAt = now
        )

        for {
          _ <- repo.save(event)
          unprocessed <- repo.fetchUnprocessed(10)
          _ = assertEquals(unprocessed.length, 1)
          _ = assertEquals(unprocessed.head.id, eventId)
          _ <- repo.markProcessed(List(eventId))
          unprocessedAfter <- repo.fetchUnprocessed(10)
          _ = assertEquals(unprocessedAfter.length, 0)
        } yield ()
      }
    }
  }
}
