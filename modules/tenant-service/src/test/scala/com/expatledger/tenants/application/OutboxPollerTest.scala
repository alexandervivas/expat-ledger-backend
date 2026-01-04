package com.expatledger.tenants.application

import java.util.UUID
import scala.concurrent.duration.*
import cats.effect.*
import com.expatledger.kernel.application.EventPublisher
import com.expatledger.kernel.domain.events.{OutboxEvent, EventType}
import com.expatledger.kernel.domain.repositories.OutboxRepository
import com.expatledger.tenants.config.OutboxConfig
import munit.CatsEffectSuite

class OutboxPollerTest extends CatsEffectSuite:

  class MockOutboxRepository extends OutboxRepository[IO]:
    var events: List[OutboxEvent] = Nil
    var processedIds: List[UUID] = Nil
    var fetchCount = 0

    override def save(event: OutboxEvent): IO[Unit] = IO.unit
    override def fetchUnprocessed(limit: Int): IO[List[OutboxEvent]] = IO {
      fetchCount += 1
      val batch = events.take(limit)
      events = events.drop(limit)
      batch
    }
    override def markProcessed(ids: List[UUID]): IO[Unit] = IO {
      processedIds = processedIds ++ ids
    }

  class MockEventPublisher extends EventPublisher[IO]:
    var publishedEvents: List[OutboxEvent] = Nil
    var failCount = 0

    override def publish(event: OutboxEvent): IO[Unit] =
      if failCount > 0 then
        failCount -= 1
        IO.raiseError(new Exception("Publish failed"))
      else
        IO { publishedEvents = publishedEvents :+ event }

  val config = OutboxConfig(
    pollInterval = 10.millis,
    batchSize = 10,
    retryInitialDelay = 10.millis,
    retryCount = 2
  )

  test("OutboxPoller should publish events and mark them as processed") {
    val outboxRepo = new MockOutboxRepository
    val publisher = new MockEventPublisher

    val event = OutboxEvent(
      id = UUID.randomUUID(),
      aggregateType = "Test",
      aggregateId = UUID.randomUUID(),
      eventType = EventType.TenantCreated,
      payload = "{}",
      avroPayload = None,
      schemaUrn = "urn:avro:schema:test",
      occurredAt = java.time.OffsetDateTime.now()
    )

    outboxRepo.events = List(event)

    val poller = new OutboxPoller[IO](outboxRepo, publisher, config)

    poller.run.take(1).compile.drain.map { _ =>
      assertEquals(publisher.publishedEvents.size, 1)
      assertEquals(publisher.publishedEvents.head.id, event.id)
      assertEquals(outboxRepo.processedIds, List(event.id))
    }
  }

  test("OutboxPoller should handle publish errors and continue") {
    val outboxRepo = new MockOutboxRepository
    val publisher = new MockEventPublisher

    val event1 = OutboxEvent(UUID.randomUUID(), "Test", UUID.randomUUID(), EventType.TenantCreated, "{}", None, "urn:test:1", java.time.OffsetDateTime.now())
    val event2 = OutboxEvent(UUID.randomUUID(), "Test", UUID.randomUUID(), EventType.TenantCreated, "{}", None, "urn:test:2", java.time.OffsetDateTime.now())

    outboxRepo.events = List(event1, event2)
    publisher.failCount = 1 // Fail the first one

    val poller = new OutboxPoller[IO](outboxRepo, publisher, config)

    poller.run.take(1).compile.drain.map { _ =>
      assertEquals(publisher.publishedEvents.size, 1)
      assertEquals(publisher.publishedEvents.head.id, event2.id)
      assertEquals(outboxRepo.processedIds, List(event2.id))
    }
  }
